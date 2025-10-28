package com.finnova.transaction_service.repository;

import com.finnova.transaction_service.model.entity.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    Flux<Transaction> findByProductId(String productId);

    Flux<Transaction> findByCustomerId(String customerId);

    Flux<Transaction> findByProductIdAndTransactionDateBetween(
            String productId,
            LocalDateTime start,
            LocalDateTime end
    );

    Mono<Transaction> findByTransactionNumber(String transactionNumber);

    Flux<Transaction> findTop10ByProductIdOrderByTransactionDateDesc(String productId);
}
