package com.finnova.digital_wallet_service.controller;

import com.finnova.digital_wallet_service.model.dto.BootCoinBalanceResponse;
import com.finnova.digital_wallet_service.model.dto.CreateBootCoinWalletRequest;
import com.finnova.digital_wallet_service.model.entity.BootCoinWallet;
import com.finnova.digital_wallet_service.service.BootCoinWalletService;
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
@RequestMapping("/api/bootcoin/wallets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BootCoin Wallets", description = "BootCoin wallet management")
public class BootCoinWalletController {

    private final BootCoinWalletService walletService;

    @PostMapping
    @Operation(summary = "Create BootCoin wallet")
    public Mono<ResponseEntity<BootCoinWallet>> createWallet(@Valid @RequestBody CreateBootCoinWalletRequest request) {
        return walletService.createWallet(request)
                .map(wallet -> ResponseEntity.status(HttpStatus.CREATED).body(wallet));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get BootCoin wallet by ID")
    public Mono<ResponseEntity<BootCoinWallet>> getWalletById(@PathVariable String id) {
        return walletService.getWalletById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get BootCoin balance")
    public Mono<ResponseEntity<BootCoinBalanceResponse>> getBalance(@PathVariable String id) {
        return walletService.getBalance(id)
                .map(balance -> ResponseEntity.ok(new BootCoinBalanceResponse(id, balance)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all BootCoin wallets")
    public Flux<BootCoinWallet> getAllWallets() {
        return walletService.getAllWallets();
    }
}
