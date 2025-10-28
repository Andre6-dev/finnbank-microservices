package com.finnova.products_service.service.impl;

import com.finnova.products_service.client.CustomerClient;
import com.finnova.products_service.client.dto.CustomerResponse;
import com.finnova.products_service.event.model.BalanceChangedEvent;
import com.finnova.products_service.event.model.PassiveProductCreatedEvent;
import com.finnova.products_service.event.publisher.ProductEventPublisher;
import com.finnova.products_service.exception.InsufficientBalanceException;
import com.finnova.products_service.exception.InvalidProductOperationException;
import com.finnova.products_service.exception.ProductNotFoundException;
import com.finnova.products_service.mapper.PassiveProductMapper;
import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreatePassiveProductRequest;
import com.finnova.products_service.model.dto.PassiveProductDto;
import com.finnova.products_service.model.dto.UpdatePassiveProductRequest;
import com.finnova.products_service.model.entity.PassiveProduct;
import com.finnova.products_service.model.enums.PassiveProductType;
import com.finnova.products_service.model.enums.ProductStatus;
import com.finnova.products_service.repository.PassiveProductRepository;
import com.finnova.products_service.service.PassiveProductService;
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
public class PassiveProductServiceImpl implements PassiveProductService {

    private final PassiveProductRepository passiveProductRepository;
    private final PassiveProductMapper passiveProductMapper;
    private final ProductCacheService cacheService;
    private final CustomerClient customerClient;
    private final ProductEventPublisher eventPublisher;

    @Override
    public Mono<PassiveProductDto> createPassiveProduct(CreatePassiveProductRequest request) {
        log.info("Creating passive product for customer: {}", request.getCustomerId());

        return customerClient.validateCustomer(request.getCustomerId())
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new InvalidProductOperationException(
                                "Customer not found or inactive: " + request.getCustomerId()));
                    }
                    return customerClient.getCustomer(request.getCustomerId());
                })
                .flatMap(customer -> validateProductCreation(customer, request))
                .flatMap(customer -> {
                    PassiveProduct product = passiveProductMapper.toEntity(request);
                    product.setAccountNumber(generateAccountNumber());
                    product.setBalance(request.getOpeningAmount());
                    product.setStatus(ProductStatus.ACTIVE);
                    product.setCurrentMonthTransactions(0);
                    product.setCreatedAt(LocalDateTime.now());
                    product.setUpdatedAt(LocalDateTime.now());

                    // Set default values based on product type
                    setDefaultValuesByProductType(product, request);

                    return passiveProductRepository.save(product);
                })
                .flatMap(savedProduct -> cacheService.cachePassiveProduct(savedProduct)
                        .then(Mono.just(savedProduct)))
                .flatMap(savedProduct -> {
                    // Publish event
                    PassiveProductCreatedEvent event = PassiveProductCreatedEvent.builder()
                            .productId(savedProduct.getId())
                            .accountNumber(savedProduct.getAccountNumber())
                            .customerId(savedProduct.getCustomerId())
                            .productType(savedProduct.getProductType())
                            .balance(savedProduct.getBalance())
                            .currency(savedProduct.getCurrency())
                            .timestamp(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishPassiveProductCreatedEvent(event)
                            .then(Mono.just(savedProduct));
                })
                .map(passiveProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Passive product created successfully: {}", dto.getId()))
                .doOnError(e -> log.error("Error creating passive product: {}", e.getMessage()));
    }

    @Override
    public Mono<PassiveProductDto> updatePassiveProduct(String id, UpdatePassiveProductRequest request) {
        log.info("Updating passive product: {}", id);

        return passiveProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with ID: " + id)))
                .flatMap(existingProduct -> {
                    if (request.getMaintenanceFee() != null) {
                        existingProduct.setMaintenanceFee(request.getMaintenanceFee());
                    }
                    if (request.getMaxTransactionsWithoutFee() != null) {
                        existingProduct.setMaxTransactionsWithoutFee(request.getMaxTransactionsWithoutFee());
                    }
                    if (request.getFeePerExtraTransaction() != null) {
                        existingProduct.setFeePerExtraTransaction(request.getFeePerExtraTransaction());
                    }
                    if (request.getMinimumDailyAverage() != null) {
                        existingProduct.setMinimumDailyAverage(request.getMinimumDailyAverage());
                    }
                    if (request.getStatus() != null) {
                        existingProduct.setStatus(request.getStatus());
                    }
                    if (request.getHolders() != null) {
                        existingProduct.setHolders(request.getHolders());
                    }
                    if (request.getAuthorizedSigners() != null) {
                        existingProduct.setAuthorizedSigners(request.getAuthorizedSigners());
                    }
                    existingProduct.setUpdatedAt(LocalDateTime.now());

                    return passiveProductRepository.save(existingProduct);
                })
                .flatMap(updatedProduct -> cacheService.cachePassiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(passiveProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Passive product updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating passive product: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deletePassiveProduct(String id) {
        log.info("Deleting passive product: {}", id);

        return passiveProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        return Mono.error(new InvalidProductOperationException(
                                "Cannot delete product with balance greater than zero"));
                    }
                    return cacheService.evictPassiveProductFromCache(id)
                            .then(passiveProductRepository.delete(product));
                })
                .doOnSuccess(v -> log.info("Passive product deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting passive product: {}", e.getMessage()));
    }

    @Override
    public Mono<PassiveProductDto> findById(String id) {
        log.debug("Finding passive product by ID: {}", id);

        return cacheService.getPassiveProductFromCache(id)
                .switchIfEmpty(
                        passiveProductRepository.findById(id)
                                .flatMap(product -> cacheService.cachePassiveProduct(product)
                                        .then(Mono.just(product)))
                )
                .map(passiveProductMapper::toDto)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with ID: " + id)));
    }

    @Override
    public Flux<PassiveProductDto> findAll() {
        log.debug("Finding all passive products");
        return passiveProductRepository.findAll()
                .map(passiveProductMapper::toDto);
    }

    @Override
    public Flux<PassiveProductDto> findByCustomerId(String customerId) {
        log.debug("Finding passive products by customer ID: {}", customerId);
        return passiveProductRepository.findByCustomerId(customerId)
                .map(passiveProductMapper::toDto);
    }

    @Override
    public Mono<PassiveProductDto> findByAccountNumber(String accountNumber) {
        log.debug("Finding passive product by account number: {}", accountNumber);
        return passiveProductRepository.findByAccountNumber(accountNumber)
                .map(passiveProductMapper::toDto)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with account number: " + accountNumber)));
    }

    @Override
    public Mono<BalanceDto> getBalance(String id) {
        log.debug("Getting balance for passive product: {}", id);

        return findById(id)
                .map(product -> BalanceDto.builder()
                        .productId(product.getId())
                        .balance(product.getBalance())
                        .currency(product.getCurrency())
                        .availableBalance(product.getBalance())
                        .build());
    }

    @Override
    public Mono<PassiveProductDto> deposit(String id, BigDecimal amount) {
        log.info("Depositing {} to passive product: {}", amount, id);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidProductOperationException(
                    "Deposit amount must be positive"));
        }

        return passiveProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getStatus() != ProductStatus.ACTIVE) {
                        return Mono.error(new InvalidProductOperationException(
                                "Product is not active"));
                    }

                    // Validate fixed-term deposit rules
                    if (product.getProductType() == PassiveProductType.FIXED_TERM) {
                        if (!canPerformFixedTermTransaction(product)) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Fixed-term account can only accept deposits on day " +
                                            product.getMovementDay()));
                        }
                    }

                    BigDecimal previousBalance = product.getBalance();
                    product.setBalance(product.getBalance().add(amount));
                    product.setCurrentMonthTransactions(product.getCurrentMonthTransactions() + 1);
                    product.setUpdatedAt(LocalDateTime.now());

                    return passiveProductRepository.save(product)
                            .flatMap(updatedProduct -> {
                                // Publish balance changed event
                                BalanceChangedEvent event = BalanceChangedEvent.builder()
                                        .productId(updatedProduct.getId())
                                        .productNumber(updatedProduct.getAccountNumber())
                                        .customerId(updatedProduct.getCustomerId())
                                        .operationType("DEPOSIT")
                                        .previousBalance(previousBalance)
                                        .newBalance(updatedProduct.getBalance())
                                        .amount(amount)
                                        .description("Deposit to account")
                                        .timestamp(LocalDateTime.now())
                                        .build();

                                return eventPublisher.publishBalanceChangedEvent(event)
                                        .then(Mono.just(updatedProduct));
                            });
                })
                .flatMap(updatedProduct -> cacheService.cachePassiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(passiveProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Deposit successful for product: {}", id))
                .doOnError(e -> log.error("Error depositing to product: {}", e.getMessage()));
    }

    @Override
    public Mono<PassiveProductDto> withdraw(String id, BigDecimal amount) {
        log.info("Withdrawing {} from passive product: {}", amount, id);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidProductOperationException(
                    "Withdrawal amount must be positive"));
        }

        return passiveProductRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Passive product not found with ID: " + id)))
                .flatMap(product -> {
                    if (product.getStatus() != ProductStatus.ACTIVE) {
                        return Mono.error(new InvalidProductOperationException(
                                "Product is not active"));
                    }

                    // Check balance
                    if (product.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient balance. Available: " + product.getBalance()));
                    }

                    // Validate fixed-term withdrawal rules
                    if (product.getProductType() == PassiveProductType.FIXED_TERM) {
                        if (!canPerformFixedTermTransaction(product)) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Fixed-term account can only accept withdrawals on day " +
                                            product.getMovementDay()));
                        }
                    }

                    // Calculate fees for extra transactions
                    BigDecimal totalAmount = amount;
                    if (product.getCurrentMonthTransactions() >= product.getMaxTransactionsWithoutFee() &&
                            product.getFeePerExtraTransaction() != null) {
                        totalAmount = totalAmount.add(product.getFeePerExtraTransaction());
                        log.info("Extra transaction fee applied: {}", product.getFeePerExtraTransaction());
                    }

                    if (product.getBalance().compareTo(totalAmount) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient balance including fees. Required: " + totalAmount));
                    }

                    BigDecimal previousBalance = product.getBalance();
                    product.setBalance(product.getBalance().subtract(totalAmount));
                    product.setCurrentMonthTransactions(product.getCurrentMonthTransactions() + 1);
                    product.setUpdatedAt(LocalDateTime.now());

                    BigDecimal totalAmountEvent = totalAmount;

                    return passiveProductRepository.save(product)
                            .flatMap(updatedProduct -> {
                                // Publish balance changed event
                                BalanceChangedEvent event = BalanceChangedEvent.builder()
                                        .productId(updatedProduct.getId())
                                        .productNumber(updatedProduct.getAccountNumber())
                                        .customerId(updatedProduct.getCustomerId())
                                        .operationType("WITHDRAWAL")
                                        .previousBalance(previousBalance)
                                        .newBalance(updatedProduct.getBalance())
                                        .amount(totalAmountEvent)
                                        .description("Withdrawal from account")
                                        .timestamp(LocalDateTime.now())
                                        .build();

                                return eventPublisher.publishBalanceChangedEvent(event)
                                        .then(Mono.just(updatedProduct));
                            });
                })
                .flatMap(updatedProduct -> cacheService.cachePassiveProduct(updatedProduct)
                        .then(Mono.just(updatedProduct)))
                .map(passiveProductMapper::toDto)
                .doOnSuccess(dto -> log.info("Withdrawal successful for product: {}", id))
                .doOnError(e -> log.error("Error withdrawing from product: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> transfer(String fromId, String toId, BigDecimal amount) {
        log.info("Transferring {} from {} to {}", amount, fromId, toId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidProductOperationException(
                    "Transfer amount must be positive"));
        }

        return withdraw(fromId, amount)
                .then(deposit(toId, amount))
                .then()
                .doOnSuccess(v -> log.info("Transfer successful from {} to {}", fromId, toId))
                .doOnError(e -> log.error("Error during transfer: {}", e.getMessage()));
    }

    /**
     * Validates product creation based on customer type and business rules.
     */
    private Mono<CustomerResponse> validateProductCreation(CustomerResponse customer,
                                                           CreatePassiveProductRequest request) {
        String customerType = customer.getCustomerType();
        PassiveProductType productType = request.getProductType();

        // Personal customer validations
        if ("PERSONAL".equals(customerType)) {
            return passiveProductRepository.findByCustomerId(customer.getId())
                    .collectList()
                    .flatMap(existingProducts -> {
                        // Personal customers can have maximum one product per type
                        long countOfType = existingProducts.stream()
                                .filter(p -> p.getProductType() == productType)
                                .count();

                        if (countOfType > 0) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Personal customers can only have one " + productType + " account"));
                        }

                        // Personal customers cannot have checking account
                        if (productType == PassiveProductType.CHECKING) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Personal customers cannot have checking accounts"));
                        }

                        return Mono.just(customer);
                    });
        }

        // Business customer validations
        if ("BUSINESS".equals(customerType)) {
            return passiveProductRepository.findByCustomerId(customer.getId())
                    .collectList()
                    .flatMap(existingProducts -> {
                        // Business customers cannot have savings or fixed-term accounts
                        if (productType == PassiveProductType.SAVINGS ||
                                productType == PassiveProductType.FIXED_TERM) {
                            return Mono.error(new InvalidProductOperationException(
                                    "Business customers cannot have " + productType + " accounts"));
                        }

                        // Business customers can have multiple checking accounts
                        return Mono.just(customer);
                    });
        }

        return Mono.just(customer);
    }

    /**
     * Sets default values based on product type.
     */
    private void setDefaultValuesByProductType(PassiveProduct product, CreatePassiveProductRequest request) {
        switch (product.getProductType()) {
            case SAVINGS:
                if (request.getMaintenanceFee() == null) {
                    product.setMaintenanceFee(BigDecimal.ZERO);
                }
                if (request.getMaxTransactionsWithoutFee() == null) {
                    product.setMaxTransactionsWithoutFee(5);
                }
                if (request.getFeePerExtraTransaction() == null) {
                    product.setFeePerExtraTransaction(new BigDecimal("2.00"));
                }
                break;

            case CHECKING:
                if (request.getMaintenanceFee() == null) {
                    product.setMaintenanceFee(new BigDecimal("10.00"));
                }
                if (request.getMaxTransactionsWithoutFee() == null) {
                    product.setMaxTransactionsWithoutFee(Integer.MAX_VALUE);
                }
                break;

            case FIXED_TERM:
                if (request.getMaintenanceFee() == null) {
                    product.setMaintenanceFee(BigDecimal.ZERO);
                }
                if (request.getMovementDay() == null) {
                    product.setMovementDay(1);
                }
                product.setMaxTransactionsWithoutFee(1);
                break;
        }
    }

    /**
     * Checks if a fixed-term transaction can be performed today.
     */
    private boolean canPerformFixedTermTransaction(PassiveProduct product) {
        if (product.getMovementDay() == null) {
            return false;
        }
        int today = LocalDateTime.now().getDayOfMonth();
        return today == product.getMovementDay();
    }

    /**
     * Generates a unique account number.
     */
    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
