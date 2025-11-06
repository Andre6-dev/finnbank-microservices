package com.finnova.digital_wallet_service.controller;

import com.finnova.digital_wallet_service.model.dto.BalanceResponse;
import com.finnova.digital_wallet_service.model.dto.CreateYankiRequest;
import com.finnova.digital_wallet_service.model.dto.LoadBalanceRequest;
import com.finnova.digital_wallet_service.model.dto.SendPaymentRequest;
import com.finnova.digital_wallet_service.model.dto.YankiResponse;
import com.finnova.digital_wallet_service.model.entity.Yanki;
import com.finnova.digital_wallet_service.service.YankiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/yanki")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Yanki", description = "Yanki digital wallet endpoints")
public class YankiController {

    private final YankiService yankiService;

    @PostMapping("/wallets")
    @Operation(summary = "Create Yanki wallet")
    public Mono<ResponseEntity<YankiResponse>> createWallet(@Valid @RequestBody CreateYankiRequest request) {
        log.info("Creating Yanki wallet for phone: {}", request.getPhoneNumber());
        return yankiService.createWallet(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/send")
    @Operation(summary = "Send payment to another Yanki user")
    public Mono<ResponseEntity<YankiResponse>> sendPayment(@Valid @RequestBody SendPaymentRequest request) {
        log.info("Sending payment from {} to {}", request.getSenderPhoneNumber(), request.getRecipientPhoneNumber());
        return yankiService.sendPayment(request)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/load")
    @Operation(summary = "Load balance from debit card")
    public Mono<ResponseEntity<YankiResponse>> loadBalance(
            @PathVariable String id,
            @Valid @RequestBody LoadBalanceRequest request
    ) {
        log.info("Loading balance for Yanki: {}", id);
        return yankiService.loadBalance(id, request)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw balance to debit card")
    public Mono<ResponseEntity<YankiResponse>> withdrawBalance(
            @PathVariable String id,
            @Valid @RequestBody LoadBalanceRequest request
    ) {
        log.info("Withdrawing balance from Yanki: {}", id);
        return yankiService.withdrawBalance(id, request)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/associate-card")
    @Operation(summary = "Associate debit card")
    public Mono<ResponseEntity<YankiResponse>> associateDebitCard(
            @PathVariable String id,
            @RequestParam String debitCardId
    ) {
        log.info("Associating debit card {} to Yanki {}", debitCardId, id);
        return yankiService.associateDebitCard(id, debitCardId)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Yanki wallet by ID")
    public Mono<ResponseEntity<YankiResponse>> getWalletById(@PathVariable String id) {
        return yankiService.getWalletById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Get Yanki wallet by phone number")
    public Mono<ResponseEntity<YankiResponse>> getWalletByPhoneNumber(@PathVariable String phoneNumber) {
        return yankiService.getWalletByPhoneNumber(phoneNumber)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get Yanki balance")
    public Mono<ResponseEntity<BalanceResponse>> getBalance(@PathVariable String id) {
        return yankiService.getBalance(id)
                .map(balance -> ResponseEntity.ok(new BalanceResponse(id, balance)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all Yanki wallets")
    public Flux<YankiResponse> getAllWallets() {
        return yankiService.getAllWallets()
                .map(this::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Yanki wallet")
    public Mono<ResponseEntity<YankiResponse>> updateWallet(
            @PathVariable String id,
            @Valid @RequestBody Yanki yanki
    ) {
        return yankiService.updateWallet(id, yanki)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Close Yanki wallet")
    public Mono<ResponseEntity<Void>> deleteWallet(@PathVariable String id) {
        return yankiService.deleteWallet(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private YankiResponse toResponse(Yanki yanki) {
        return YankiResponse.builder()
                .id(yanki.getId())
                .documentType(yanki.getDocumentType())
                .documentNumber(yanki.getDocumentNumber())
                .phoneNumber(yanki.getPhoneNumber())
                .imei(yanki.getImei())
                .email(yanki.getEmail())
                .balance(yanki.getBalance())
                .associatedDebitCardId(yanki.getAssociatedDebitCardId())
                .status(yanki.getStatus())
                .createdAt(yanki.getCreatedAt())
                .updatedAt(yanki.getUpdatedAt())
                .build();
    }
}
