package com.finnova.transaction_service.service;

import com.finnova.transaction_service.client.ProductClient;
import com.finnova.transaction_service.event.publisher.TransactionEventPublisher;
import com.finnova.transaction_service.exception.InsufficientBalanceException;
import com.finnova.transaction_service.exception.InvalidTransactionException;
import com.finnova.transaction_service.exception.ProductNotFoundException;
import com.finnova.transaction_service.model.dto.ProductDto;
import com.finnova.transaction_service.model.dto.TransferRequest;
import com.finnova.transaction_service.model.entity.Transaction;
import com.finnova.transaction_service.model.enums.TransactionStatus;
import com.finnova.transaction_service.model.enums.TransactionType;
import com.finnova.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final ProductClient productClient;
    private final TransactionEventPublisher eventPublisher;

    /**
     * Process transfer between own accounts
     */
//    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackTransfer")
//    @TimeLimiter(name = "productService")
    public Mono<Transaction> transferBetweenOwnAccounts(TransferRequest request) {
        log.info("Processing transfer between own accounts: {} -> {}",
                request.getSourceProductId(), request.getDestinationProductId());

        return validateAndExecuteTransfer(request, true);
    }

    /**
     * Process transfer to third party accounts
     */
//    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackTransfer")
//    @TimeLimiter(name = "productService")
    public Mono<Transaction> transferToThirdParty(TransferRequest request) {
        log.info("Processing transfer to third party: {} -> {}",
                request.getSourceProductId(), request.getDestinationProductId());

        return validateAndExecuteTransfer(request, false);
    }

    /**
     * Validate and execute the transfer
     */
    private Mono<Transaction> validateAndExecuteTransfer(TransferRequest request, boolean ownAccounts) {
        String transactionNumber = generateTransactionNumber();

        return Mono.zip(
                        productClient.getProduct(request.getSourceProductId())
                                .switchIfEmpty(Mono.error(new ProductNotFoundException("Source product not found"))),
                        productClient.getProduct(request.getDestinationProductId())
                                .switchIfEmpty(Mono.error(new ProductNotFoundException("Destination product not found")))
                )
                .flatMap(tuple -> {
                    var sourceProduct = tuple.getT1();
                    var destProduct = tuple.getT2();

                    // Validar que sean cuentas del mismo banco (ambos existen en products-service)
                    if (sourceProduct == null || destProduct == null) {
                        return Mono.error(new InvalidTransactionException("Both accounts must belong to the same bank"));
                    }

                    // Validar que no sean la misma cuenta
                    if (request.getSourceProductId().equals(request.getDestinationProductId())) {
                        return Mono.error(new InvalidTransactionException("Cannot transfer to the same account"));
                    }

                    // Si es entre cuentas propias, validar que pertenezcan al mismo cliente
                    if (ownAccounts && !sourceProduct.getCustomerId().equals(destProduct.getCustomerId())) {
                        return Mono.error(new InvalidTransactionException("Accounts must belong to the same customer"));
                    }

                    // Validar saldo suficiente
                    if (sourceProduct.getBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient balance in source account"));
                    }

                    // Crear transacciones (débito y crédito)
                    Transaction debitTransaction = createDebitTransaction(
                            transactionNumber, sourceProduct, request, destProduct.getCustomerId()
                    );

                    Transaction creditTransaction = createCreditTransaction(
                            transactionNumber, destProduct, request, sourceProduct.getCustomerId()
                    );

                    // Ejecutar transferencia
                    return executeTransfer(debitTransaction, creditTransaction, sourceProduct, destProduct, request);
                })
                .onErrorResume(ex -> {
                    log.error("Transfer failed: {}", ex.getMessage());
                    return eventPublisher.publishTransferFailed(transactionNumber, ex.getMessage())
                            .then(Mono.error(ex));
                });
    }

    /**
     * Execute the transfer by updating balances and saving transactions
     */
    private Mono<Transaction> executeTransfer(
            Transaction debitTransaction,
            Transaction creditTransaction,
            Object sourceProduct,
            Object destProduct,
            TransferRequest request
    ) {
        BigDecimal newSourceBalance = ((ProductDto) sourceProduct)
                .getBalance().subtract(request.getAmount());
        BigDecimal newDestBalance = ((ProductDto) destProduct)
                .getBalance().add(request.getAmount());

        // Guardar transacción de débito
        return transactionRepository.save(debitTransaction)
                .flatMap(savedDebit -> {
                    // Actualizar balance de cuenta origen
                    return productClient.updateBalance(request.getSourceProductId(), newSourceBalance)
                            .flatMap(updatedSource -> {
                                savedDebit.setStatus(TransactionStatus.COMPLETED);
                                savedDebit.setBalanceAfter(newSourceBalance);
                                savedDebit.setUpdatedAt(LocalDateTime.now());

                                // Guardar transacción de crédito
                                return transactionRepository.save(creditTransaction)
                                        .flatMap(savedCredit -> {
                                            // Actualizar balance de cuenta destino
                                            return productClient.updateBalance(request.getDestinationProductId(), newDestBalance)
                                                    .flatMap(updatedDest -> {
                                                        savedCredit.setStatus(TransactionStatus.COMPLETED);
                                                        savedCredit.setBalanceAfter(newDestBalance);
                                                        savedCredit.setUpdatedAt(LocalDateTime.now());

                                                        // Guardar ambas transacciones actualizadas
                                                        return transactionRepository.save(savedDebit)
                                                                .then(transactionRepository.save(savedCredit))
                                                                .then(eventPublisher.publishTransferCompleted(savedDebit, savedCredit))
                                                                .thenReturn(savedDebit);
                                                    });
                                        });
                            });
                });
    }

    /**
     * Create debit transaction (TRANSFER_OUT)
     */
    private Transaction createDebitTransaction(
            String transactionNumber,
            Object sourceProduct,
            TransferRequest request,
            String destCustomerId
    ) {
        var product = (ProductDto) sourceProduct;

        return Transaction.builder()
                .transactionNumber(transactionNumber + "-OUT")
                .customerId(product.getCustomerId())
                .productId(request.getSourceProductId())
                .productType(product.getProductType())
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .balanceBefore(product.getBalance())
                .commission(BigDecimal.ZERO)
                .destinationProductId(request.getDestinationProductId())
                .destinationCustomerId(destCustomerId)
                .description(request.getDescription())
                .status(TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create credit transaction (TRANSFER_IN)
     */
    private Transaction createCreditTransaction(
            String transactionNumber,
            Object destProduct,
            TransferRequest request,
            String sourceCustomerId
    ) {
        var product = (ProductDto) destProduct;

        return Transaction.builder()
                .transactionNumber(transactionNumber + "-IN")
                .customerId(product.getCustomerId())
                .productId(request.getDestinationProductId())
                .productType(product.getProductType())
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(request.getAmount())
                .balanceBefore(product.getBalance())
                .commission(BigDecimal.ZERO)
                .destinationProductId(request.getSourceProductId())
                .destinationCustomerId(sourceCustomerId)
                .description(request.getDescription())
                .status(TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Helper methods

    private String generateTransactionNumber() {
        return "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Fallback method

    public Mono<Transaction> fallbackTransfer(TransferRequest request, Exception ex) {
        log.error("Fallback: transfer failed", ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }
}
