package com.finnova.digital_wallet_service.service;

import com.finnova.digital_wallet_service.client.CardClient;
import com.finnova.digital_wallet_service.exception.InsufficientBalanceException;
import com.finnova.digital_wallet_service.exception.InvalidOperationException;
import com.finnova.digital_wallet_service.exception.WalletNotFoundException;
import com.finnova.digital_wallet_service.model.dto.CreateYankiRequest;
import com.finnova.digital_wallet_service.model.dto.LoadBalanceRequest;
import com.finnova.digital_wallet_service.model.dto.SendPaymentRequest;
import com.finnova.digital_wallet_service.model.entity.Yanki;
import com.finnova.digital_wallet_service.model.enums.WalletStatus;
import com.finnova.digital_wallet_service.repository.YankiRepository;
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
public class YankiService {

    private final YankiRepository yankiRepository;
    private final CardClient cardClient;

    /**
     * Create Yanki wallet
     */
    public Mono<Yanki> createWallet(CreateYankiRequest request) {
        log.info("Creating Yanki wallet for phone: {}", request.getPhoneNumber());

        // Validate phone number uniqueness
        return yankiRepository.existsByPhoneNumber(request.getPhoneNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidOperationException("Phone number already registered"));
                    }

                    // Validate document number uniqueness
                    return yankiRepository.existsByDocumentNumber(request.getDocumentNumber())
                            .flatMap(docExists -> {
                                if (docExists) {
                                    return Mono.error(new InvalidOperationException("Document number already registered"));
                                }

                                Yanki yanki = Yanki.builder()
                                        .documentType(request.getDocumentType())
                                        .documentNumber(request.getDocumentNumber())
                                        .phoneNumber(request.getPhoneNumber())
                                        .imei(request.getImei())
                                        .email(request.getEmail())
                                        .balance(BigDecimal.ZERO)
                                        .status(WalletStatus.ACTIVE)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                                return yankiRepository.save(yanki);
                            });
                });
    }

    /**
     * Send payment to another Yanki user
     */
    public Mono<Yanki> sendPayment(SendPaymentRequest request) {
        log.info("Sending payment from {} to {}", request.getSenderPhoneNumber(), request.getRecipientPhoneNumber());

        // Validate sender and recipient are different
        if (request.getSenderPhoneNumber().equals(request.getRecipientPhoneNumber())) {
            return Mono.error(new InvalidOperationException("Cannot send payment to yourself"));
        }

        return Mono.zip(
                yankiRepository.findByPhoneNumber(request.getSenderPhoneNumber())
                        .switchIfEmpty(Mono.error(new WalletNotFoundException("Sender wallet not found"))),
                yankiRepository.findByPhoneNumber(request.getRecipientPhoneNumber())
                        .switchIfEmpty(Mono.error(new WalletNotFoundException("Recipient wallet not found")))
        ).flatMap(tuple -> {
            Yanki sender = tuple.getT1();
            Yanki recipient = tuple.getT2();

            // Validate sender has sufficient balance
            if (sender.getBalance().compareTo(request.getAmount()) < 0) {
                return Mono.error(new InsufficientBalanceException("Insufficient balance"));
            }

            // Validate sender is active
            if (sender.getStatus() != WalletStatus.ACTIVE) {
                return Mono.error(new InvalidOperationException("Sender wallet is not active"));
            }

            // Validate recipient is active
            if (recipient.getStatus() != WalletStatus.ACTIVE) {
                return Mono.error(new InvalidOperationException("Recipient wallet is not active"));
            }

            // Debit sender
            sender.setBalance(sender.getBalance().subtract(request.getAmount()));
            sender.setUpdatedAt(LocalDateTime.now());

            // Credit recipient
            recipient.setBalance(recipient.getBalance().add(request.getAmount()));
            recipient.setUpdatedAt(LocalDateTime.now());

            // Save both
            return yankiRepository.save(sender)
                    .then(yankiRepository.save(recipient))
                    .thenReturn(sender);
        });
    }

    /**
     * Load balance from associated debit card
     */
    public Mono<Yanki> loadBalance(String yankiId, LoadBalanceRequest request) {
        log.info("Loading balance for Yanki: {}", yankiId);

        return yankiRepository.findById(yankiId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")))
                .flatMap(yanki -> {
                    if (yanki.getAssociatedDebitCardId() == null) {
                        return Mono.error(new InvalidOperationException("No debit card associated"));
                    }

                    // Withdraw from main account of debit card
                    return cardClient.withdrawFromDebitCard(yanki.getAssociatedDebitCardId(), request.getAmount())
                            .then(Mono.defer(() -> {
                                yanki.setBalance(yanki.getBalance().add(request.getAmount()));
                                yanki.setUpdatedAt(LocalDateTime.now());
                                return yankiRepository.save(yanki);
                            }));
                });
    }

    /**
     * Withdraw balance to associated debit card
     */
    public Mono<Yanki> withdrawBalance(String yankiId, LoadBalanceRequest request) {
        log.info("Withdrawing balance from Yanki: {}", yankiId);

        return yankiRepository.findById(yankiId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")))
                .flatMap(yanki -> {
                    if (yanki.getAssociatedDebitCardId() == null) {
                        return Mono.error(new InvalidOperationException("No debit card associated"));
                    }

                    // Validate sufficient balance
                    if (yanki.getBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient balance"));
                    }

                    // Debit Yanki balance
                    yanki.setBalance(yanki.getBalance().subtract(request.getAmount()));
                    yanki.setUpdatedAt(LocalDateTime.now());

                    // Deposit to main account via card service
                    return yankiRepository.save(yanki)
                            .flatMap(savedYanki ->
                                    cardClient.depositToDebitCard(yanki.getAssociatedDebitCardId(), request.getAmount())
                                            .thenReturn(savedYanki)
                            );
                });
    }

    /**
     * Associate debit card
     */
    public Mono<Yanki> associateDebitCard(String yankiId, String debitCardId) {
        log.info("Associating debit card {} to Yanki {}", debitCardId, yankiId);

        return yankiRepository.findById(yankiId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")))
                .flatMap(yanki -> {
                    // Validate debit card exists
                    return cardClient.getDebitCard(debitCardId)
                            .flatMap(card -> {
                                yanki.setAssociatedDebitCardId(debitCardId);
                                yanki.setUpdatedAt(LocalDateTime.now());
                                return yankiRepository.save(yanki);
                            });
                });
    }

    /**
     * Get wallet by ID
     */
    public Mono<Yanki> getWalletById(String id) {
        return yankiRepository.findById(id)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")));
    }

    /**
     * Get wallet by phone number
     */
    public Mono<Yanki> getWalletByPhoneNumber(String phoneNumber) {
        return yankiRepository.findByPhoneNumber(phoneNumber)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")));
    }

    /**
     * Get balance
     */
    public Mono<BigDecimal> getBalance(String yankiId) {
        return yankiRepository.findById(yankiId)
                .map(Yanki::getBalance)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")));
    }

    /**
     * Get all wallets
     */
    public Flux<Yanki> getAllWallets() {
        return yankiRepository.findAll();
    }

    /**
     * Update wallet
     */
    public Mono<Yanki> updateWallet(String id, Yanki yanki) {
        return yankiRepository.findById(id)
                .flatMap(existing -> {
                    existing.setEmail(yanki.getEmail());
                    existing.setStatus(yanki.getStatus());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return yankiRepository.save(existing);
                })
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Yanki wallet not found")));
    }

    /**
     * Delete wallet
     */
    public Mono<Void> deleteWallet(String id) {
        return yankiRepository.findById(id)
                .flatMap(yanki -> {
                    yanki.setStatus(WalletStatus.CLOSED);
                    yanki.setUpdatedAt(LocalDateTime.now());
                    return yankiRepository.save(yanki);
                })
                .then();
    }
}
