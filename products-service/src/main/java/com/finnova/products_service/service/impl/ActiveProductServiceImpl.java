package com.finnova.products_service.service.impl;

import com.finnova.products_service.client.CustomerClient;
import com.finnova.products_service.client.dto.CustomerResponse;
import com.finnova.products_service.event.model.ActiveProductCreatedEvent;
import com.finnova.products_service.event.model.BalanceChangedEvent;
import com.finnova.products_service.event.publisher.ProductEventPublisher;
import com.finnova.products_service.exception.InvalidProductOperationException;
import com.finnova.products_service.exception.ProductNotFoundException;
import com.finnova.products_service.mapper.ActiveProductMapper;
import com.finnova.products_service.model.dto.ActiveProductDto;
import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreateActiveProductRequest;
import com.finnova.products_service.model.dto.UpdateActiveProductRequest;
import com.finnova.products_service.model.entity.ActiveProduct;
import com.finnova.products_service.model.enums.ActiveProductType;
import com.finnova.products_service.model.enums.ProductStatus;
import com.finnova.products_service.repository.ActiveProductRepository;
import com.finnova.products_service.service.ActiveProductService;
import com.finnova.products_service.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveProductServiceImpl implements ActiveProductService {
    private final ActiveProductRepository activeProductRepository;
    private final ActiveProductMapper activeProductMapper;
    private final ProductCacheService cacheService;
    private final CustomerClient customerClient;
    private final ProductEventPublisher eventPublisher;

    @Override
    public Mono<ActiveProductDto> createActiveProduct(CreateActiveProductRequest request) {
        log.info("Creating active product for customer: {}", request.getCustomerId());

        return customerClient.validateCustomer(request.getCustomerId())
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new InvalidProductOperationException(
                                "Customer not found or inactive: " + request.getCustomerId()));
                    }
                    return customerClient.getCustomer(request.getCustomerId());
                })
                .flatMap(customer -> hasOverdueDebt(customer.getId())
                        .flatMap(hasDebt -> {
                            if (Boolean.TRUE.equals(hasDebt)) {
                                return Mono.error(new InvalidProductOperationException(
                                        "Customer has overdue debt and cannot open new credit products"));
                            }
                            return validateProductCreation(customer, request);
                        }))
                .flatMap(customer -> {
                    ActiveProduct product = activeProductMapper.toEntity(request);
                    product.setCreditNumber(generateCreditNumber());
                    product.setAvailableCredit(request.getCreditLimit());
                    product.setUsedCredit(BigDecimal.ZERO);
                    product.setOutstandingBalance(BigDecimal.ZERO);
                    product.setOverdueAmount(BigDecimal.ZERO);
                    product.setHasOverdueDebt(false);
                    product.setStatus(ProductStatus.ACTIVE);
                    product.setCreatedAt(LocalDateTime.now());
                    product.setUpdatedAt(LocalDateTime.now());

                    // Set minimum payment (typically 5% of outstanding balance)
                    product.setMinimumPayment(BigDecimal.ZERO);

                    return activeProductRepository.save(product);
                })
                .flatMap(savedProduct -> cacheService.cacheActiveProduct(savedProduct)
                        .then(Mono.just(savedProduct)))
                .flatMap(savedProduct -> {
                    // Publish event
                    ActiveProductCreatedEvent event = ActiveProductCreatedEvent.builder()
                            .productId(savedProduct.getId())
                            .creditNumber(savedProduct.getCreditNumber())
                            .customerId(savedProduct.getCustomerId())
                            .productType(savedProduct.getProductType())
                            .creditLimit(savedProduct.getCreditLimit())
                            .currency(savedProduct.getCurrency())
                            .timestamp(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishActiveProductCreatedEvent(event)
                            .then(Mono.just(savedProduct));
                })
                .map(activeProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Active product created successfully: {}", dto.getId()))
                .doOnError(e -> log.error("Error creating active product: {}", e.getMessage()));
    }

    @Override
    public Mono<ActiveProductDto> updateActiveProduct(String id, UpdateActiveProductRequest request) {
        log.info("Updating active product: {}", id);

        return activeProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with ID: " + id)))
                .flatMap(existingProduct -> {
                    if (request.getCreditLimit() != null) {
                        // Validate new credit limit
                        if (request.getCreditLimit().compareTo(existingProduct.getUsedCredit()) < 0) {
                            return Mono.error(new InvalidProductOperationException(
                                    "New credit limit cannot be less than used credit"));
                        }
                        BigDecimal difference = request.getCreditLimit().subtract(existingProduct.getCreditLimit());
                        existingProduct.setCreditLimit(request.getCreditLimit());
                        existingProduct.setAvailableCredit(existingProduct.getAvailableCredit().add(difference));
                    }
                    if (request.getInterestRate() != null) {
                        existingProduct.setInterestRate(request.getInterestRate());
                    }
                    if (request.getPaymentDueDate() != null) {
                        existingProduct.setPaymentDueDate(request.getPaymentDueDate());
                    }
                    if (request.getStatus() != null) {
                        existingProduct.setStatus(request.getStatus());
                    }
                    existingProduct.setUpdatedAt(LocalDateTime.now());

                    return activeProductRepository.save(existingProduct);
                })
                .flatMap(updatedProduct -> cacheService.cacheActiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(activeProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Active product updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating active product: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deleteActiveProduct(String id) {
        log.info("Deleting active product: {}", id);

        return activeProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                        return Mono.error(new InvalidProductOperationException(
                                "Cannot delete product with outstanding balance"));
                    }
                    return cacheService.evictActiveProductFromCache(id)
                            .then(activeProductRepository.delete(product));
                })
                .doOnSuccess(v -> log.info("Active product deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting active product: {}", e.getMessage()));
    }

    @Override
    public Mono<ActiveProductDto> findById(String id) {
        log.debug("Finding active product by ID: {}", id);

        return cacheService.getActiveProductFromCache(id)
                .switchIfEmpty(
                        activeProductRepository.findById(id)
                                .flatMap(product -> cacheService.cacheActiveProduct(product)
                                        .then(Mono.just(product)))
                )
                .map(activeProductMapper::toDto)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with ID: " + id)));
    }

    @Override
    public Flux<ActiveProductDto> findAll() {
        log.debug("Finding all active products");
        return activeProductRepository.findAll()
                .map(activeProductMapper::toDto);
    }

    @Override
    public Flux<ActiveProductDto> findByCustomerId(String customerId) {
        log.debug("Finding active products by customer ID: {}", customerId);
        return activeProductRepository.findByCustomerId(customerId)
                .map(activeProductMapper::toDto);
    }

    @Override
    public Mono<ActiveProductDto> findByCreditNumber(String creditNumber) {
        log.debug("Finding active product by credit number: {}", creditNumber);
        return activeProductRepository.findByCreditNumber(creditNumber)
                .map(activeProductMapper::toDto)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with credit number: " + creditNumber)));
    }

    @Override
    public Mono<BalanceDto> getAvailableCredit(String id) {
        log.debug("Getting available credit for active product: {}", id);

        return findById(id)
                .map(product -> BalanceDto.builder()
                        .productId(product.getId())
                        .balance(product.getCreditLimit())
                        .currency(product.getCurrency())
                        .availableBalance(product.getAvailableCredit())
                        .build());
    }

    @Override
    public Mono<ActiveProductDto> makeCharge(String id, BigDecimal amount) {
        log.info("Making charge of {} to active product: {}", amount, id);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidProductOperationException(
                    "Charge amount must be positive"));
        }

        return activeProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getStatus() != ProductStatus.ACTIVE) {
                        return Mono.error(new InvalidProductOperationException(
                                "Product is not active"));
                    }

                    // Check available credit
                    if (product.getAvailableCredit().compareTo(amount) < 0) {
                        return Mono.error(new InvalidProductOperationException(
                                "Insufficient credit. Available: " + product.getAvailableCredit()));
                    }

                    BigDecimal previousAvailable = product.getAvailableCredit();

                    // Update balances
                    product.setUsedCredit(product.getUsedCredit().add(amount));
                    product.setAvailableCredit(product.getAvailableCredit().subtract(amount));
                    product.setOutstandingBalance(product.getOutstandingBalance().add(amount));

                    // Calculate minimum payment (5% of outstanding balance)
                    BigDecimal minimumPayment = product.getOutstandingBalance()
                            .multiply(new BigDecimal("0.05"))
                            .setScale(2, BigDecimal.ROUND_HALF_UP);
                    product.setMinimumPayment(minimumPayment);

                    product.setUpdatedAt(LocalDateTime.now());

                    return activeProductRepository.save(product)
                            .flatMap(updatedProduct -> {
                                // Publish balance changed event
                                BalanceChangedEvent event = BalanceChangedEvent.builder()
                                        .productId(updatedProduct.getId())
                                        .productNumber(updatedProduct.getCreditNumber())
                                        .customerId(updatedProduct.getCustomerId())
                                        .operationType("CHARGE")
                                        .previousBalance(previousAvailable)
                                        .newBalance(updatedProduct.getAvailableCredit())
                                        .amount(amount)
                                        .description("Charge to credit")
                                        .timestamp(LocalDateTime.now())
                                        .build();

                                return eventPublisher.publishBalanceChangedEvent(event)
                                        .then(Mono.just(updatedProduct));
                            });
                })
                .flatMap(updatedProduct -> cacheService.cacheActiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(activeProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Charge successful for product: {}", id))
                .doOnError(e -> log.error("Error making charge to product: {}", e.getMessage()));
    }

    @Override
    public Mono<ActiveProductDto> makePayment(String id, BigDecimal amount) {
        log.info("Making payment of {} to active product: {}", amount, id);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidProductOperationException(
                    "Payment amount must be positive"));
        }

        return activeProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Active product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getStatus() == ProductStatus.BLOCKED) {
                        return Mono.error(new InvalidProductOperationException(
                                "Product is blocked"));
                    }

                    // Payment cannot exceed outstanding balance
                    if (amount.compareTo(product.getOutstandingBalance()) > 0) {
                        return Mono.error(new InvalidProductOperationException(
                                "Payment amount exceeds outstanding balance"));
                    }

                    BigDecimal previousAvailable = product.getAvailableCredit();

                    // Update balances
                    product.setOutstandingBalance(product.getOutstandingBalance().subtract(amount));
                    product.setUsedCredit(product.getUsedCredit().subtract(amount));
                    product.setAvailableCredit(product.getAvailableCredit().add(amount));

                    // Update overdue amount if applicable
                    if (product.getOverdueAmount().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal overduePayment = amount.min(product.getOverdueAmount());
                        product.setOverdueAmount(product.getOverdueAmount().subtract(overduePayment));
                    }

                    // Update overdue debt status
                    if (product.getOverdueAmount().compareTo(BigDecimal.ZERO) == 0) {
                        product.setHasOverdueDebt(false);
                        if (product.getStatus() == ProductStatus.OVERDUE) {
                            product.setStatus(ProductStatus.ACTIVE);
                        }
                    }

                    // Recalculate minimum payment
                    if (product.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal minimumPayment = product.getOutstandingBalance()
                                .multiply(new BigDecimal("0.05"))
                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                        product.setMinimumPayment(minimumPayment);
                    } else {
                        product.setMinimumPayment(BigDecimal.ZERO);
                    }

                    product.setUpdatedAt(LocalDateTime.now());

                    return activeProductRepository.save(product)
                            .flatMap(updatedProduct -> {
                                // Publish balance changed event
                                BalanceChangedEvent event = BalanceChangedEvent.builder()
                                        .productId(updatedProduct.getId())
                                        .productNumber(updatedProduct.getCreditNumber())
                                        .customerId(updatedProduct.getCustomerId())
                                        .operationType("PAYMENT")
                                        .previousBalance(previousAvailable)
                                        .newBalance(updatedProduct.getAvailableCredit())
                                        .amount(amount)
                                        .description("Payment to credit")
                                        .timestamp(LocalDateTime.now())
                                        .build();

                                return eventPublisher.publishBalanceChangedEvent(event)
                                        .then(Mono.just(updatedProduct));
                            });
                })
                .flatMap(updatedProduct -> cacheService.cacheActiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(activeProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Payment successful for product: {}", id))
                .doOnError(e -> log.error("Error making payment to product: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> hasOverdueDebt(String customerId) {
        log.debug("Checking overdue debt for customer: {}", customerId);

        return activeProductRepository.findByCustomerIdAndHasOverdueDebt(customerId, true)
                .hasElements();
    }

    /**
     * Validates product creation based on customer type and business rules.
     */
    private Mono<CustomerResponse> validateProductCreation(CustomerResponse customer,
                                                           CreateActiveProductRequest request) {
        String customerType = customer.getCustomerType();
        ActiveProductType productType = request.getProductType();

        // Personal customer validations
        if ("PERSONAL".equals(customerType)) {
            return activeProductRepository.findByCustomerId(customer.getId())
                    .collectList()
                    .flatMap(existingProducts -> {
                        // Personal customers can only have one personal loan
                        if (productType == ActiveProductType.PERSONAL_LOAN) {
                            long loanCount = existingProducts.stream()
                                    .filter(p -> p.getProductType() == ActiveProductType.PERSONAL_LOAN)
                                    .count();

                            if (loanCount > 0) {
                                return Mono.error(new InvalidProductOperationException(
                                        "Personal customers can only have one personal loan"));
                            }
                        }

                        // Personal customers cannot have business loans
                        if (productType == ActiveProductType.BUSINESS_LOAN) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Personal customers cannot have business loans"));
                        }

                        // VIP customers must have credit card
                        if ("VIP".equals(customer.getProfileType())) {
                            boolean hasCreditCard = existingProducts.stream()
                                    .anyMatch(p -> p.getProductType() == ActiveProductType.CREDIT_CARD);

                            if (!hasCreditCard && productType != ActiveProductType.CREDIT_CARD) {
                                return Mono.error(new InvalidProductOperationException(
                                        "VIP customers must have a credit card to open other accounts"));
                            }
                        }

                        return Mono.just(customer);
                    });
        }

        // Business customer validations
        if ("BUSINESS".equals(customerType)) {
            return activeProductRepository.findByCustomerId(customer.getId())
                    .collectList()
                    .flatMap(existingProducts -> {
                        // Business customers cannot have personal loans
                        if (productType == ActiveProductType.PERSONAL_LOAN) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Business customers cannot have personal loans"));
                        }

                        // PYME customers must have credit card
                        if ("PYME".equals(customer.getProfileType())) {
                            boolean hasCreditCard = existingProducts.stream()
                                    .anyMatch(p -> p.getProductType() == ActiveProductType.CREDIT_CARD);

                            if (!hasCreditCard && productType != ActiveProductType.CREDIT_CARD) {
                                return Mono.error(new InvalidProductOperationException(
                                        "PYME customers must have a credit card to open other accounts"));
                            }
                        }

                        // Business customers can have multiple business loans
                        return Mono.just(customer);
                    });
        }

        return Mono.just(customer);
    }

    /**
     * Generates a unique credit number.
     */
    private String generateCreditNumber() {
        return "CRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
