package com.finnova.transaction_service.service;

import com.finnova.transaction_service.client.CustomerClient;
import com.finnova.transaction_service.client.ProductClient;
import com.finnova.transaction_service.event.publisher.TransactionEventPublisher;
import com.finnova.transaction_service.exception.InsufficientBalanceException;
import com.finnova.transaction_service.exception.InvalidTransactionException;
import com.finnova.transaction_service.exception.ProductNotFoundException;
import com.finnova.transaction_service.model.dto.CreditChargeRequest;
import com.finnova.transaction_service.model.dto.DepositRequest;
import com.finnova.transaction_service.model.dto.PaymentRequest;
import com.finnova.transaction_service.model.dto.ProductDto;
import com.finnova.transaction_service.model.dto.WithdrawalRequest;
import com.finnova.transaction_service.model.entity.Transaction;
import com.finnova.transaction_service.model.enums.TransactionStatus;
import com.finnova.transaction_service.model.enums.TransactionType;
import com.finnova.transaction_service.repository.TransactionRepository;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final TransactionEventPublisher eventPublisher;

    // ========== DEPOSIT ==========

    /**
     * Process a deposit transaction
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackDeposit")
    @TimeLimiter(name = "productService")
    public Mono<Transaction> deposit(DepositRequest request) {
        log.info("Processing deposit for product: {}", request.getProductId());

        return productClient.getProduct(request.getProductId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found")))
                .flatMap(product -> {
                    // Validate product is a passive account (SAVINGS, CHECKING, FIXED_TERM)
                    if (!isPassiveProduct(product.getProductType())) {
                        return Mono.error(new InvalidTransactionException("Deposits can only be made to bank accounts"));
                    }

                    // Validate fixed term deposit day
                    if ("FIXED_TERM".equals(product.getProductType())) {
                        // TODO: Validate specific day of month for fixed term deposits
                        // For now, we allow deposits on any day
                    }

                    BigDecimal newBalance = product.getBalance().add(request.getAmount());

                    Transaction transaction = Transaction.builder()
                            .transactionNumber(generateTransactionNumber("DEP"))
                            .customerId(product.getCustomerId())
                            .productId(request.getProductId())
                            .productType(product.getProductType())
                            .transactionType(TransactionType.DEPOSIT)
                            .amount(request.getAmount())
                            .balanceBefore(product.getBalance())
                            .balanceAfter(newBalance)
                            .commission(BigDecimal.ZERO)
                            .description(request.getDescription())
                            .status(TransactionStatus.PENDING)
                            .transactionDate(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return transactionRepository.save(transaction)
                            .flatMap(savedTransaction ->
                                    eventPublisher.publishTransactionCreated(savedTransaction)
                                            .then(productClient.updateBalance(request.getProductId(), newBalance))
                                            .flatMap(updatedProduct -> {
                                                savedTransaction.setStatus(TransactionStatus.COMPLETED);
                                                savedTransaction.setUpdatedAt(LocalDateTime.now());
                                                return transactionRepository.save(savedTransaction);
                                            })
                            )
                            .flatMap(completedTransaction ->
                                    eventPublisher.publishTransactionCompleted(completedTransaction)
                                            .thenReturn(completedTransaction)
                            )
                            .onErrorResume(error -> {
                                transaction.setStatus(TransactionStatus.FAILED);
                                transaction.setDescription(transaction.getDescription() + " - Error: " + error.getMessage());
                                return transactionRepository.save(transaction)
                                        .flatMap(failedTx ->
                                                eventPublisher.publishTransactionFailed(failedTx, error.getMessage())
                                                        .then(Mono.error(error))
                                        );
                            });
                });
    }

    // ========== WITHDRAWAL ==========

    /**
     * Process a withdrawal transaction
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackWithdrawal")
    @TimeLimiter(name = "productService")
    public Mono<Transaction> withdrawal(WithdrawalRequest request) {
        log.info("Processing withdrawal for product: {}", request.getProductId());

        return productClient.getProduct(request.getProductId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found")))
                .flatMap(product -> {
                    // Validate product is a passive account
                    if (!isPassiveProduct(product.getProductType())) {
                        return Mono.error(new InvalidTransactionException("Withdrawals can only be made from bank accounts"));
                    }

                    // Validate sufficient balance
                    if (product.getBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient balance"));
                    }

                    // Validate fixed term withdrawal day
                    if ("FIXED_TERM".equals(product.getProductType())) {
                        // TODO: Validate specific day of month for fixed term withdrawals
                    }

                    // Calculate commission based on monthly transaction count
                    return getMonthlyTransactionCount(request.getProductId())
                            .flatMap(transactionCount -> {
                                BigDecimal commission = calculateCommission(product, transactionCount);
                                BigDecimal totalAmount = request.getAmount().add(commission);

                                // Validate balance including commission
                                if (product.getBalance().compareTo(totalAmount) < 0) {
                                    return Mono.error(new InsufficientBalanceException(
                                            "Insufficient balance including commission"));
                                }

                                BigDecimal newBalance = product.getBalance().subtract(totalAmount);

                                Transaction transaction = Transaction.builder()
                                        .transactionNumber(generateTransactionNumber("WTH"))
                                        .customerId(product.getCustomerId())
                                        .productId(request.getProductId())
                                        .productType(product.getProductType())
                                        .transactionType(TransactionType.WITHDRAWAL)
                                        .amount(request.getAmount())
                                        .balanceBefore(product.getBalance())
                                        .balanceAfter(newBalance)
                                        .commission(commission)
                                        .description(request.getDescription())
                                        .status(TransactionStatus.PENDING)
                                        .transactionDate(LocalDateTime.now())
                                        .createdAt(LocalDateTime.now())
                                        .build();

                                return transactionRepository.save(transaction)
                                        .flatMap(savedTransaction ->
                                                eventPublisher.publishTransactionCreated(savedTransaction)
                                                        .then(productClient.updateBalance(request.getProductId(), newBalance))
                                                        .flatMap(updatedProduct -> {
                                                            savedTransaction.setStatus(TransactionStatus.COMPLETED);
                                                            savedTransaction.setUpdatedAt(LocalDateTime.now());
                                                            return transactionRepository.save(savedTransaction);
                                                        })
                                        )
                                        .flatMap(completedTransaction ->
                                                eventPublisher.publishTransactionCompleted(completedTransaction)
                                                        .thenReturn(completedTransaction)
                                        )
                                        .onErrorResume(error -> {
                                            transaction.setStatus(TransactionStatus.FAILED);
                                            transaction.setDescription(transaction.getDescription() + " - Error: " + error.getMessage());
                                            return transactionRepository.save(transaction)
                                                    .flatMap(failedTx ->
                                                            eventPublisher.publishTransactionFailed(failedTx, error.getMessage())
                                                                    .then(Mono.error(error))
                                                    );
                                        });
                            });
                });
    }

    // ========== PAYMENT (Credit/Credit Card) ==========

    /**
     * Process a payment to credit or credit card
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackPayment")
    @TimeLimiter(name = "productService")
    public Mono<Transaction> payment(PaymentRequest request) {
        log.info("Processing payment for product: {}", request.getProductId());

        return productClient.getProduct(request.getProductId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found")))
                .flatMap(product -> {
                    // Validate product is active (CREDIT, CREDIT_CARD)
                    if (!isActiveProduct(product.getProductType())) {
                        return Mono.error(new InvalidTransactionException("Payments can only be made to credit products"));
                    }

                    // Validate payment amount
                    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new InvalidTransactionException("Payment amount must be positive"));
                    }

                    // For credit cards: validate not exceeding debt
                    BigDecimal currentDebt = product.getCreditLimit().subtract(product.getAvailableBalance());
                    if (request.getAmount().compareTo(currentDebt) > 0) {
                        return Mono.error(new InvalidTransactionException(
                                "Payment amount cannot exceed current debt"));
                    }

                    BigDecimal newAvailableBalance = product.getAvailableBalance().add(request.getAmount());

                    // Determine who is paying (for third party payments - Proyecto III)
                    String payerCustomerId = request.getPayerCustomerId() != null
                            ? request.getPayerCustomerId()
                            : product.getCustomerId();

                    Transaction transaction = Transaction.builder()
                            .transactionNumber(generateTransactionNumber("PAY"))
                            .customerId(payerCustomerId)
                            .productId(request.getProductId())
                            .productType(product.getProductType())
                            .transactionType(TransactionType.PAYMENT)
                            .amount(request.getAmount())
                            .balanceBefore(product.getAvailableBalance())
                            .balanceAfter(newAvailableBalance)
                            .commission(BigDecimal.ZERO)
                            .description(request.getDescription())
                            .status(TransactionStatus.PENDING)
                            .transactionDate(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return transactionRepository.save(transaction)
                            .flatMap(savedTransaction ->
                                    eventPublisher.publishTransactionCreated(savedTransaction)
                                            .then(productClient.updateBalance(request.getProductId(), newAvailableBalance))
                                            .flatMap(updatedProduct -> {
                                                savedTransaction.setStatus(TransactionStatus.COMPLETED);
                                                savedTransaction.setUpdatedAt(LocalDateTime.now());
                                                return transactionRepository.save(savedTransaction);
                                            })
                            )
                            .flatMap(completedTransaction ->
                                    eventPublisher.publishTransactionCompleted(completedTransaction)
                                            .thenReturn(completedTransaction)
                            )
                            .onErrorResume(error -> {
                                transaction.setStatus(TransactionStatus.FAILED);
                                transaction.setDescription(transaction.getDescription() + " - Error: " + error.getMessage());
                                return transactionRepository.save(transaction)
                                        .flatMap(failedTx ->
                                                eventPublisher.publishTransactionFailed(failedTx, error.getMessage())
                                                        .then(Mono.error(error))
                                        );
                            });
                });
    }

    // ========== CREDIT CHARGE ==========

    /**
     * Process a credit card charge (consumption)
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreditCharge")
    @TimeLimiter(name = "productService")
    public Mono<Transaction> creditCharge(CreditChargeRequest request) {
        log.info("Processing credit charge for card: {}", request.getCreditCardId());

        return productClient.getProduct(request.getCreditCardId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Credit card not found")))
                .flatMap(product -> {
                    // Validate product is a credit card
                    if (!"CREDIT_CARD".equals(product.getProductType())) {
                        return Mono.error(new InvalidTransactionException("Product is not a credit card"));
                    }

                    // Validate available balance (credit limit)
                    if (product.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient credit limit. Available: " + product.getAvailableBalance()));
                    }

                    BigDecimal newAvailableBalance = product.getAvailableBalance().subtract(request.getAmount());

                    Transaction transaction = Transaction.builder()
                            .transactionNumber(generateTransactionNumber("CHG"))
                            .customerId(product.getCustomerId())
                            .productId(request.getCreditCardId())
                            .productType(product.getProductType())
                            .transactionType(TransactionType.CREDIT_CHARGE)
                            .amount(request.getAmount())
                            .balanceBefore(product.getAvailableBalance())
                            .balanceAfter(newAvailableBalance)
                            .commission(BigDecimal.ZERO)
                            .description(request.getDescription() + " - Merchant: " + request.getMerchantName())
                            .status(TransactionStatus.PENDING)
                            .transactionDate(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return transactionRepository.save(transaction)
                            .flatMap(savedTransaction ->
                                    eventPublisher.publishTransactionCreated(savedTransaction)
                                            .then(productClient.updateBalance(request.getCreditCardId(), newAvailableBalance))
                                            .flatMap(updatedProduct -> {
                                                savedTransaction.setStatus(TransactionStatus.COMPLETED);
                                                savedTransaction.setUpdatedAt(LocalDateTime.now());
                                                return transactionRepository.save(savedTransaction);
                                            })
                            )
                            .flatMap(completedTransaction ->
                                    eventPublisher.publishTransactionCompleted(completedTransaction)
                                            .thenReturn(completedTransaction)
                            )
                            .onErrorResume(error -> {
                                transaction.setStatus(TransactionStatus.FAILED);
                                transaction.setDescription(transaction.getDescription() + " - Error: " + error.getMessage());
                                return transactionRepository.save(transaction)
                                        .flatMap(failedTx ->
                                                eventPublisher.publishTransactionFailed(failedTx, error.getMessage())
                                                        .then(Mono.error(error))
                                        );
                            });
                });
    }

    // ========== QUERIES ==========

    /**
     * Get all transactions for a product
     */
    public Flux<Transaction> getTransactionsByProduct(String productId) {
        log.info("Fetching transactions for product: {}", productId);
        return transactionRepository.findByProductId(productId);
    }

    /**
     * Get all transactions for a customer
     */
    public Flux<Transaction> getTransactionsByCustomer(String customerId) {
        log.info("Fetching transactions for customer: {}", customerId);
        return transactionRepository.findByCustomerId(customerId);
    }

    /**
     * Get transaction by ID
     */
    public Mono<Transaction> getTransactionById(String id) {
        log.info("Fetching transaction: {}", id);
        return transactionRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")));
    }

    /**
     * Get last 10 transactions for a product
     */
    public Flux<Transaction> getLast10Transactions(String productId) {
        log.info("Fetching last 10 transactions for product: {}", productId);
        return transactionRepository.findTop10ByProductIdOrderByTransactionDateDesc(productId);
    }

    /**
     * Get product balance (delegates to product service)
     */
    public Mono<BigDecimal> getProductBalance(String productId) {
        log.info("Fetching balance for product: {}", productId);
        return productClient.getProduct(productId)
                .map(ProductDto::getBalance)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found")));
    }

    /**
     * Get all transactions
     */
    public Flux<Transaction> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAll();
    }

    // ========== CRUD Operations ==========

    /**
     * Update transaction
     */
    public Mono<Transaction> updateTransaction(String id, Transaction transaction) {
        log.info("Updating transaction: {}", id);
        return transactionRepository.findById(id)
                .flatMap(existingTransaction -> {
                    // Update only allowed fields
                    existingTransaction.setDescription(transaction.getDescription());
                    existingTransaction.setStatus(transaction.getStatus());
                    existingTransaction.setUpdatedAt(LocalDateTime.now());
                    return transactionRepository.save(existingTransaction);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")));
    }

    /**
     * Delete transaction
     */
    public Mono<Void> deleteTransaction(String id) {
        log.info("Deleting transaction: {}", id);
        return transactionRepository.findById(id)
                .flatMap(transaction -> {
                    // Only allow deletion of FAILED or CANCELLED transactions
                    if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                        return Mono.error(new InvalidTransactionException(
                                "Cannot delete completed transactions"));
                    }
                    return transactionRepository.deleteById(id);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")));
    }

    // ========== HELPER METHODS ==========

    /**
     * Get monthly transaction count for a product
     */
    private Mono<Long> getMonthlyTransactionCount(String productId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        return transactionRepository
                .findByProductIdAndTransactionDateBetween(productId, startOfMonth, endOfMonth)
                .filter(tx -> tx.getTransactionType() == TransactionType.DEPOSIT
                        || tx.getTransactionType() == TransactionType.WITHDRAWAL)
                .filter(tx -> tx.getStatus() == TransactionStatus.COMPLETED)
                .count();
    }

    /**
     * Calculate commission based on product type and transaction count
     */
    private BigDecimal calculateCommission(ProductDto product, Long transactionCount) {
        // Default commission rate
        BigDecimal commissionRate = BigDecimal.ZERO;

        // Get max free transactions from product (default values if not set)
        Integer maxFreeTransactions = product.getMaxFreeTransactions() != null
                ? product.getMaxFreeTransactions()
                : getDefaultMaxFreeTransactions(product.getProductType());

        // If exceeded max free transactions, charge commission
        if (transactionCount >= maxFreeTransactions) {
            switch (product.getProductType()) {
                case "SAVINGS":
                    commissionRate = new BigDecimal("1.00"); // S/. 1.00 per transaction
                    break;
                case "CHECKING":
                    commissionRate = new BigDecimal("2.00"); // S/. 2.00 per transaction
                    break;
                case "FIXED_TERM":
                    commissionRate = new BigDecimal("0.00"); // No commission
                    break;
                default:
                    commissionRate = BigDecimal.ZERO;
            }
        }

        return commissionRate;
    }

    /**
     * Get default max free transactions by product type
     */
    private Integer getDefaultMaxFreeTransactions(String productType) {
        return switch (productType) {
            case "SAVINGS" -> 10; // Savings: max 10 free monthly transactions
            case "CHECKING" -> Integer.MAX_VALUE; // Checking: unlimited transactions
            case "FIXED_TERM" -> 1; // Fixed term: 1 transaction per month
            default -> 0;
        };
    }

    /**
     * Check if product is passive (account)
     */
    private boolean isPassiveProduct(String productType) {
        return "SAVINGS".equals(productType)
                || "CHECKING".equals(productType)
                || "FIXED_TERM".equals(productType);
    }

    /**
     * Check if product is active (credit)
     */
    private boolean isActiveProduct(String productType) {
        return "CREDIT".equals(productType)
                || "CREDIT_CARD".equals(productType);
    }

    /**
     * Generate transaction number with prefix
     */
    private String generateTransactionNumber(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ========== FALLBACK METHODS ==========

    public Mono<Transaction> fallbackDeposit(DepositRequest request, Exception ex) {
        log.error("Fallback: deposit failed for product: {}", request.getProductId(), ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }

    public Mono<Transaction> fallbackWithdrawal(WithdrawalRequest request, Exception ex) {
        log.error("Fallback: withdrawal failed for product: {}", request.getProductId(), ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }

    public Mono<Transaction> fallbackPayment(PaymentRequest request, Exception ex) {
        log.error("Fallback: payment failed for product: {}", request.getProductId(), ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }

    public Mono<Transaction> fallbackCreditCharge(CreditChargeRequest request, Exception ex) {
        log.error("Fallback: credit charge failed for card: {}", request.getCreditCardId(), ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }
}
