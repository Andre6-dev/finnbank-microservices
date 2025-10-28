package com.finnova.transaction_service.controller;


import com.finnova.transaction_service.model.dto.BalanceResponse;
import com.finnova.transaction_service.model.dto.CreditChargeRequest;
import com.finnova.transaction_service.model.dto.DepositRequest;
import com.finnova.transaction_service.model.dto.PaymentRequest;
import com.finnova.transaction_service.model.dto.TransactionResponse;
import com.finnova.transaction_service.model.dto.TransferRequest;
import com.finnova.transaction_service.model.dto.WithdrawalRequest;
import com.finnova.transaction_service.model.entity.Transaction;
import com.finnova.transaction_service.service.TransactionService;
import com.finnova.transaction_service.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferService transferService;

    // ========== DEPOSIT ==========

    @PostMapping("/deposit")
//    @Operation(summary = "Process a deposit")
    public Mono<ResponseEntity<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("Received deposit request for product: {}", request.getProductId());
        return transactionService.deposit(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing deposit", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== WITHDRAWAL ==========

    @PostMapping("/withdrawal")
//    @Operation(summary = "Process a withdrawal")
    public Mono<ResponseEntity<TransactionResponse>> withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        log.info("Received withdrawal request for product: {}", request.getProductId());
        return transactionService.withdrawal(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing withdrawal", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== PAYMENT (Credit/Credit Card) ==========

    @PostMapping("/payment")
//    @Operation(summary = "Process a payment to credit or credit card")
    public Mono<ResponseEntity<TransactionResponse>> payment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for product: {}", request.getProductId());
        return transactionService.payment(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing payment", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== CREDIT CHARGE (Consumo de tarjeta) ==========

    @PostMapping("/credit-charge")
//    @Operation(summary = "Process a credit card charge")
    public Mono<ResponseEntity<TransactionResponse>> creditCharge(@Valid @RequestBody CreditChargeRequest request) {
        log.info("Received credit charge request for card: {}", request.getCreditCardId());
        return transactionService.creditCharge(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing credit charge", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== TRANSFERS ==========

    @PostMapping("/transfer/own")
//    @Operation(summary = "Transfer between own accounts")
    public Mono<ResponseEntity<TransactionResponse>> transferOwnAccounts(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request between own accounts: {} -> {}",
                request.getSourceProductId(), request.getDestinationProductId());
        return transferService.transferBetweenOwnAccounts(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing transfer", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @PostMapping("/transfer/third-party")
//    @Operation(summary = "Transfer to third party account")
    public Mono<ResponseEntity<TransactionResponse>> transferThirdParty(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request to third party: {} -> {}",
                request.getSourceProductId(), request.getDestinationProductId());
        return transferService.transferToThirdParty(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error processing transfer", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== QUERIES ==========

    @GetMapping("/{id}")
//    @Operation(summary = "Get transaction by ID")
    public Mono<ResponseEntity<TransactionResponse>> getTransactionById(@PathVariable String id) {
        log.info("Fetching transaction: {}", id);
        return transactionService.getTransactionById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
//    @Operation(summary = "Get all transactions for a product")
    public Flux<TransactionResponse> getTransactionsByProduct(@PathVariable String productId) {
        log.info("Fetching transactions for product: {}", productId);
        return transactionService.getTransactionsByProduct(productId)
                .map(this::toResponse);
    }

    @GetMapping("/customer/{customerId}")
//    @Operation(summary = "Get all transactions for a customer")
    public Flux<TransactionResponse> getTransactionsByCustomer(@PathVariable String customerId) {
        log.info("Fetching transactions for customer: {}", customerId);
        return transactionService.getTransactionsByCustomer(customerId)
                .map(this::toResponse);
    }

    @GetMapping("/product/{productId}/last10")
//    @Operation(summary = "Get last 10 transactions for a product")
    public Flux<TransactionResponse> getLast10Transactions(@PathVariable String productId) {
        log.info("Fetching last 10 transactions for product: {}", productId);
        return transactionService.getLast10Transactions(productId)
                .map(this::toResponse);
    }

    @GetMapping("/product/{productId}/balance")
//    @Operation(summary = "Get current balance for a product")
    public Mono<ResponseEntity<BalanceResponse>> getBalance(@PathVariable String productId) {
        log.info("Fetching balance for product: {}", productId);
        return transactionService.getProductBalance(productId)
                .map(balance -> ResponseEntity.ok(BalanceResponse.builder()
                        .productId(productId)
                        .balance(balance)
                        .build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ========== CRUD Operations ==========

    @GetMapping
//    @Operation(summary = "Get all transactions")
    public Flux<TransactionResponse> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionService.getAllTransactions()
                .map(this::toResponse);
    }

    @PutMapping("/{id}")
//    @Operation(summary = "Update transaction")
    public Mono<ResponseEntity<TransactionResponse>> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody Transaction transaction
    ) {
        log.info("Updating transaction: {}", id);
        return transactionService.updateTransaction(id, transaction)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
//    @Operation(summary = "Delete transaction")
    public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable String id) {
        log.info("Deleting transaction: {}", id);
        return transactionService.deleteTransaction(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ========== Helper Methods ==========

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionNumber(transaction.getTransactionNumber())
                .customerId(transaction.getCustomerId())
                .productId(transaction.getProductId())
                .productType(transaction.getProductType())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .commission(transaction.getCommission())
                .destinationProductId(transaction.getDestinationProductId())
                .destinationCustomerId(transaction.getDestinationCustomerId())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
