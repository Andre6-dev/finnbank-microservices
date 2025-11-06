package com.finnova.digital_wallet_service.controller;

import com.finnova.digital_wallet_service.model.dto.AcceptBuyRequestDto;
import com.finnova.digital_wallet_service.model.dto.BootCoinTransactionResponse;
import com.finnova.digital_wallet_service.model.dto.CreateBuyRequestDto;
import com.finnova.digital_wallet_service.model.entity.BootCoinTransaction;
import com.finnova.digital_wallet_service.service.BootCoinTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bootcoin/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BootCoin Transactions", description = "BootCoin P2P transactions")
public class BootCoinTransactionController {

    private final BootCoinTransactionService transactionService;

    @PostMapping("/buy-request")
    @Operation(summary = "Create buy request")
    public Mono<ResponseEntity<BootCoinTransactionResponse>> createBuyRequest(
            @Valid @RequestBody CreateBuyRequestDto request
    ) {
        return transactionService.createBuyRequest(request)
                .map(this::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/{transactionNumber}/accept")
    @Operation(summary = "Accept buy request")
    public Mono<ResponseEntity<BootCoinTransactionResponse>> acceptBuyRequest(
            @PathVariable String transactionNumber,
            @Valid @RequestBody AcceptBuyRequestDto request
    ) {
        return transactionService.acceptBuyRequest(transactionNumber, request)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{transactionNumber}/complete")
    @Operation(summary = "Complete transaction")
    public Mono<ResponseEntity<BootCoinTransactionResponse>> completeTransaction(
            @PathVariable String transactionNumber
    ) {
        return transactionService.completeTransaction(transactionNumber)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{transactionNumber}")
    @Operation(summary = "Get transaction by number")
    public Mono<ResponseEntity<BootCoinTransactionResponse>> getTransactionByNumber(
            @PathVariable String transactionNumber
    ) {
        return transactionService.getTransactionByNumber(transactionNumber)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/wallet/{walletId}")
    @Operation(summary = "Get transactions by wallet")
    public Flux<BootCoinTransactionResponse> getTransactionsByWallet(@PathVariable String walletId) {
        return transactionService.getTransactionsByWallet(walletId)
                .map(this::toResponse);
    }

    private BootCoinTransactionResponse toResponse(BootCoinTransaction tx) {
        return BootCoinTransactionResponse.builder()
                .id(tx.getId())
                .transactionNumber(tx.getTransactionNumber())
                .buyerWalletId(tx.getBuyerWalletId())
                .sellerWalletId(tx.getSellerWalletId())
                .solesAmount(tx.getSolesAmount())
                .bootCoinAmount(tx.getBootCoinAmount())
                .exchangeRate(tx.getExchangeRate())
                .paymentMethod(tx.getPaymentMethod())
                .paymentDetails(tx.getPaymentDetails())
                .status(tx.getStatus())
                .transactionDate(tx.getTransactionDate())
                .acceptedDate(tx.getAcceptedDate())
                .completedDate(tx.getCompletedDate())
                .build();
    }
}
