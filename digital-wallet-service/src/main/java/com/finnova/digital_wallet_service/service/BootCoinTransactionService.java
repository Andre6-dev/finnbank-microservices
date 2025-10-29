package com.finnova.digital_wallet_service.service;

import com.finnova.digital_wallet_service.exception.InsufficientBalanceException;
import com.finnova.digital_wallet_service.exception.InvalidOperationException;
import com.finnova.digital_wallet_service.exception.TransactionNotFoundException;
import com.finnova.digital_wallet_service.model.dto.AcceptBuyRequestDto;
import com.finnova.digital_wallet_service.model.dto.CreateBuyRequestDto;
import com.finnova.digital_wallet_service.model.entity.BootCoinTransaction;
import com.finnova.digital_wallet_service.model.entity.BootCoinWallet;
import com.finnova.digital_wallet_service.model.enums.TransactionStatus;
import com.finnova.digital_wallet_service.repository.BootCoinTransactionRepository;
import com.finnova.digital_wallet_service.repository.BootCoinWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootCoinTransactionService {

    private final BootCoinTransactionRepository transactionRepository;
    private final BootCoinWalletRepository walletRepository;
    private final ExchangeRateService exchangeRateService;

    /**
     * Create buy request (buyer initiates)
     */
    public Mono<BootCoinTransaction> createBuyRequest(CreateBuyRequestDto request) {
        log.info("Creating BootCoin buy request for wallet: {}", request.getBuyerWalletId());

        return walletRepository.findById(request.getBuyerWalletId())
                .switchIfEmpty(Mono.error(new InvalidOperationException("Buyer wallet not found")))
                .flatMap(buyerWallet -> exchangeRateService.getCurrentRate()
                        .flatMap(rate -> {
                            // Calculate BootCoin amount
                            BigDecimal bootCoinAmount = request.getSolesAmount()
                                    .divide(rate.getBuyRate(), 8, RoundingMode.HALF_UP);

                            BootCoinTransaction transaction = BootCoinTransaction.builder()
                                    .transactionNumber(generateTransactionNumber())
                                    .buyerWalletId(request.getBuyerWalletId())
                                    .solesAmount(request.getSolesAmount())
                                    .bootCoinAmount(bootCoinAmount)
                                    .exchangeRate(rate.getBuyRate())
                                    .paymentMethod(request.getPaymentMethod())
                                    .paymentDetails(request.getPaymentDetails())
                                    .status(TransactionStatus.PENDING)
                                    .transactionDate(LocalDateTime.now())
                                    .build();

                            return transactionRepository.save(transaction);
                        })
                );
    }

    /**
     * Accept buy request (seller accepts)
     */
    public Mono<BootCoinTransaction> acceptBuyRequest(String transactionNumber, AcceptBuyRequestDto request) {
        log.info("Accepting BootCoin buy request: {}", transactionNumber);

        return transactionRepository.findByTransactionNumber(transactionNumber)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found")))
                .flatMap(transaction -> {
                    if (transaction.getStatus() != TransactionStatus.PENDING) {
                        return Mono.error(new InvalidOperationException("Transaction is not pending"));
                    }

                    // Validate seller wallet exists
                    return walletRepository.findById(request.getSellerWalletId())
                            .switchIfEmpty(Mono.error(new InvalidOperationException("Seller wallet not found")))
                            .flatMap(sellerWallet -> {
                                // Validate seller has enough BootCoins
                                if (sellerWallet.getBootCoinBalance().compareTo(transaction.getBootCoinAmount()) < 0) {
                                    return Mono.error(new InsufficientBalanceException(
                                            "Seller has insufficient BootCoin balance"));
                                }

                                transaction.setSellerWalletId(request.getSellerWalletId());
                                transaction.setStatus(TransactionStatus.ACCEPTED);
                                transaction.setAcceptedDate(LocalDateTime.now());

                                return transactionRepository.save(transaction);
                            });
                });
    }

    /**
     * Complete transaction (after payment validation)
     */
    public Mono<BootCoinTransaction> completeTransaction(String transactionNumber) {
        log.info("Completing BootCoin transaction: {}", transactionNumber);

        return transactionRepository.findByTransactionNumber(transactionNumber)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found")))
                .flatMap(transaction -> {
                    if (transaction.getStatus() != TransactionStatus.ACCEPTED) {
                        return Mono.error(new InvalidOperationException("Transaction must be accepted first"));
                    }

                    // Get buyer and seller wallets
                    return Mono.zip(
                            walletRepository.findById(transaction.getBuyerWalletId()),
                            walletRepository.findById(transaction.getSellerWalletId())
                    ).flatMap(tuple -> {
                        BootCoinWallet buyerWallet = tuple.getT1();
                        BootCoinWallet sellerWallet = tuple.getT2();

                        // Validate seller still has enough
                        if (sellerWallet.getBootCoinBalance().compareTo(transaction.getBootCoinAmount()) < 0) {
                            return Mono.error(new InsufficientBalanceException(
                                    "Seller no longer has sufficient BootCoin balance"));
                        }

                        // Transfer BootCoins: seller -> buyer
                        sellerWallet.setBootCoinBalance(
                                sellerWallet.getBootCoinBalance().subtract(transaction.getBootCoinAmount()));
                        sellerWallet.setUpdatedAt(LocalDateTime.now());

                        buyerWallet.setBootCoinBalance(
                                buyerWallet.getBootCoinBalance().add(transaction.getBootCoinAmount()));
                        buyerWallet.setUpdatedAt(LocalDateTime.now());

                        // Update transaction status
                        transaction.setStatus(TransactionStatus.COMPLETED);
                        transaction.setCompletedDate(LocalDateTime.now());

                        // Save all
                        return walletRepository.save(sellerWallet)
                                .then(walletRepository.save(buyerWallet))
                                .then(transactionRepository.save(transaction));
                    });
                });
    }

    /**
     * Get transaction by number
     */
    public Mono<BootCoinTransaction> getTransactionByNumber(String transactionNumber) {
        return transactionRepository.findByTransactionNumber(transactionNumber)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found")));
    }

    /**
     * Get transactions by wallet
     */
    public Flux<BootCoinTransaction> getTransactionsByWallet(String walletId) {
        return Flux.merge(
                transactionRepository.findByBuyerWalletId(walletId),
                transactionRepository.findBySellerWalletId(walletId)
        ).sort((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()));
    }

    private String generateTransactionNumber() {
        return "BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

