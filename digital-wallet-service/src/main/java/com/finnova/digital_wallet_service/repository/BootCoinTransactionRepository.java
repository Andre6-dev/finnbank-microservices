package com.finnova.digital_wallet_service.repository;

import com.finnova.digital_wallet_service.model.entity.BootCoinTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BootCoinTransactionRepository extends ReactiveMongoRepository<BootCoinTransaction, String> {

    Mono<BootCoinTransaction> findByTransactionNumber(String transactionNumber);

    Flux<BootCoinTransaction> findByBuyerWalletId(String buyerWalletId);

    Flux<BootCoinTransaction> findBySellerWalletId(String sellerWalletId);
}
