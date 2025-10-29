package com.finnova.digital_wallet_service.repository;

import com.finnova.digital_wallet_service.model.entity.Yanki;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface YankiRepository extends ReactiveMongoRepository<Yanki, String> {

    Mono<Yanki> findByPhoneNumber(String phoneNumber);

    Mono<Yanki> findByDocumentNumber(String documentNumber);

    Mono<Boolean> existsByPhoneNumber(String phoneNumber);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
