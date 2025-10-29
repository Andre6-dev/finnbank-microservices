package com.finnova.digital_wallet_service.service;

import com.finnova.digital_wallet_service.exception.InvalidOperationException;
import com.finnova.digital_wallet_service.exception.WalletNotFoundException;
import com.finnova.digital_wallet_service.model.dto.CreateBootCoinWalletRequest;
import com.finnova.digital_wallet_service.model.entity.BootCoinWallet;
import com.finnova.digital_wallet_service.model.enums.WalletStatus;
import com.finnova.digital_wallet_service.repository.BootCoinWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootCoinWalletService {

    private final BootCoinWalletRepository walletRepository;

    /**
     * Create BootCoin wallet
     */
    public Mono<BootCoinWallet> createWallet(CreateBootCoinWalletRequest request) {
        log.info("Creating BootCoin wallet for document: {}", request.getDocumentNumber());

        return walletRepository.existsByDocumentNumber(request.getDocumentNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidOperationException("Document number already registered"));
                    }

                    BootCoinWallet wallet = BootCoinWallet.builder()
                            .documentType(request.getDocumentType())
                            .documentNumber(request.getDocumentNumber())
                            .phoneNumber(request.getPhoneNumber())
                            .email(request.getEmail())
                            .bootCoinBalance(BigDecimal.ZERO)
                            .status(WalletStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return walletRepository.save(wallet);
                });
    }

    /**
     * Get wallet by ID
     */
    public Mono<BootCoinWallet> getWalletById(String id) {
        return walletRepository.findById(id)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("BootCoin wallet not found")));
    }

    /**
     * Get balance
     */
    public Mono<BigDecimal> getBalance(String walletId) {
        return walletRepository.findById(walletId)
                .map(BootCoinWallet::getBootCoinBalance)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("BootCoin wallet not found")));
    }

    /**
     * Get all wallets
     */
    public Flux<BootCoinWallet> getAllWallets() {
        return walletRepository.findAll();
    }

    /**
     * Update wallet balance
     */
    public Mono<BootCoinWallet> updateBalance(String walletId, BigDecimal newBalance) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> {
                    wallet.setBootCoinBalance(newBalance);
                    wallet.setUpdatedAt(LocalDateTime.now());
                    return walletRepository.save(wallet);
                })
                .switchIfEmpty(Mono.error(new WalletNotFoundException("BootCoin wallet not found")));
    }
}
