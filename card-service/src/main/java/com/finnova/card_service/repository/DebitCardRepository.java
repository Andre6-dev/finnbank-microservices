package com.finnova.card_service.repository;

import com.finnova.card_service.model.entity.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {

    Flux<DebitCard> findByCustomerId(String customerId);

    Mono<DebitCard> findByCardNumber(String cardNumber);

    Mono<Boolean> existsByCardNumber(String cardNumber);
}
