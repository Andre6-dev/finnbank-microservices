package com.finnova.digital_wallet_service.repository;

import com.finnova.digital_wallet_service.model.entity.BootCoinWallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BootCoinWalletRepository extends ReactiveMongoRepository<BootCoinWallet, String> {

    Mono<BootCoinWallet> findByDocumentNumber(String documentNumber);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
