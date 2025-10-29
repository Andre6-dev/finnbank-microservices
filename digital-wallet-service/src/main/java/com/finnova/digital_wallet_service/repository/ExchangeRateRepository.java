package com.finnova.digital_wallet_service.repository;

import com.finnova.digital_wallet_service.model.entity.ExchangeRate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ExchangeRateRepository extends ReactiveMongoRepository<ExchangeRate, String> {

    Mono<ExchangeRate> findFirstByIsActiveTrueOrderByEffectiveDateDesc();
}
