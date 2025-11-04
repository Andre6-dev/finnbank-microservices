package com.finnova.card_service.controller;

import com.finnova.card_service.model.dto.AssociateAccountRequest;
import com.finnova.card_service.model.dto.BalanceResponse;
import com.finnova.card_service.model.dto.CreateDebitCardRequest;
import com.finnova.card_service.model.dto.DebitCardResponse;
import com.finnova.card_service.model.dto.PaymentRequest;
import com.finnova.card_service.model.dto.TransactionDto;
import com.finnova.card_service.model.dto.WithdrawalRequest;
import com.finnova.card_service.model.entity.DebitCard;
import com.finnova.card_service.service.DebitCardService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/debit-cards")
@RequiredArgsConstructor
@Slf4j
public class DebitCardController {

    private final DebitCardService debitCardService;

    // ========== CREATE ==========

    @PostMapping
    @Operation(summary = "Create a new debit card")
    public Mono<ResponseEntity<DebitCardResponse>> createDebitCard(
            @Valid @RequestBody CreateDebitCardRequest request
    ) {
        log.info("Received request to create debit card for customer: {}", request.getCustomerId());
        return debitCardService.createDebitCard(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Error creating debit card", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== ASSOCIATE ACCOUNTS ==========

    @PostMapping("/{id}/associate-account")
    @Operation(summary = "Associate an account to the debit card")
    public Mono<ResponseEntity<DebitCardResponse>> associateAccount(
            @PathVariable String id,
            @Valid @RequestBody AssociateAccountRequest request
    ) {
        log.info("Associating account {} to card {}", request.getAccountId(), id);
        return debitCardService.associateAccount(id, request)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/main-account")
    @Operation(summary = "Set main account for the debit card")
    public Mono<ResponseEntity<DebitCardResponse>> setMainAccount(
            @PathVariable String id,
            @RequestParam String accountId
    ) {
        log.info("Setting main account {} for card {}", accountId, id);
        return debitCardService.setMainAccount(id, accountId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ========== PAYMENT & WITHDRAWAL ==========

    @PostMapping("/{id}/payment")
    @Operation(summary = "Make payment with debit card")
    public Mono<ResponseEntity<TransactionDto>> makePayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRequest request
    ) {
        log.info("Processing payment for card: {}", id);
        return debitCardService.makePayment(id, request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error processing payment", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @PostMapping("/{id}/withdrawal")
    @Operation(summary = "Make withdrawal with debit card")
    public Mono<ResponseEntity<TransactionDto>> makeWithdrawal(
            @PathVariable String id,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        log.info("Processing withdrawal for card: {}", id);
        return debitCardService.makeWithdrawal(id, request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error processing withdrawal", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ========== QUERIES ==========

    @GetMapping("/{id}")
    @Operation(summary = "Get debit card by ID")
    public Mono<ResponseEntity<DebitCardResponse>> getDebitCardById(@PathVariable String id) {
        log.info("Fetching debit card: {}", id);
        return debitCardService.getDebitCardById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all debit cards for a customer")
    public Flux<DebitCardResponse> getDebitCardsByCustomer(@PathVariable String customerId) {
        log.info("Fetching debit cards for customer: {}", customerId);
        return debitCardService.getDebitCardsByCustomer(customerId)
                .map(this::toResponse);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get main account balance")
    public Mono<ResponseEntity<BalanceResponse>> getMainAccountBalance(@PathVariable String id) {
        log.info("Fetching balance for card: {}", id);
        return debitCardService.getMainAccountBalance(id)
                .map(balance -> ResponseEntity.ok(BalanceResponse.builder()
                        .cardId(id)
                        .balance(balance)
                        .build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Get last 10 movements")
    public Flux<TransactionDto> getLast10Movements(@PathVariable String id) {
        log.info("Fetching last 10 movements for card: {}", id);
        return debitCardService.getLast10Movements(id);
    }

    @GetMapping
    @Operation(summary = "Get all debit cards")
    public Flux<DebitCardResponse> getAllDebitCards() {
        log.info("Fetching all debit cards");
        return debitCardService.getAllDebitCards()
                .map(this::toResponse);
    }

    // ========== UPDATE & DELETE ==========

    @PutMapping("/{id}")
    @Operation(summary = "Update debit card")
    public Mono<ResponseEntity<DebitCardResponse>> updateDebitCard(
            @PathVariable String id,
            @Valid @RequestBody DebitCard debitCard
    ) {
        log.info("Updating debit card: {}", id);
        return debitCardService.updateDebitCard(id, debitCard)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel debit card")
    public Mono<ResponseEntity<Void>> deleteDebitCard(@PathVariable String id) {
        log.info("Cancelling debit card: {}", id);
        return debitCardService.deleteDebitCard(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ========== HELPER METHODS ==========

    private DebitCardResponse toResponse(DebitCard card) {
        return DebitCardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .associatedAccountIds(card.getAssociatedAccountIds())
                .mainAccountId(card.getMainAccountId())
                .cvv(card.getCvv())
                .expirationDate(card.getExpirationDate())
                .cardholderName(card.getCardholderName())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
