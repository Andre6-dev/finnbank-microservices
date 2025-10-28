package com.finnova.products_service.service;

import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreatePassiveProductRequest;
import com.finnova.products_service.model.dto.PassiveProductDto;
import com.finnova.products_service.model.dto.UpdatePassiveProductRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PassiveProductService {

    /**
     * Creates a new passive product.
     *
     * @param request the create passive product request
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> createPassiveProduct(CreatePassiveProductRequest request);

    /**
     * Updates a passive product.
     *
     * @param id the product ID
     * @param request the update passive product request
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> updatePassiveProduct(String id, UpdatePassiveProductRequest request);

    /**
     * Deletes a passive product.
     *
     * @param id the product ID
     * @return Mono of Void
     */
    Mono<Void> deletePassiveProduct(String id);

    /**
     * Finds a passive product by ID.
     *
     * @param id the product ID
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> findById(String id);

    /**
     * Finds all passive products.
     *
     * @return Flux of PassiveProductDto
     */
    Flux<PassiveProductDto> findAll();

    /**
     * Finds passive products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of PassiveProductDto
     */
    Flux<PassiveProductDto> findByCustomerId(String customerId);

    /**
     * Finds a passive product by account number.
     *
     * @param accountNumber the account number
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> findByAccountNumber(String accountNumber);

    /**
     * Gets balance of a passive product.
     *
     * @param id the product ID
     * @return Mono of BalanceDto
     */
    Mono<BalanceDto> getBalance(String id);

    /**
     * Deposits money into a passive product.
     *
     * @param id the product ID
     * @param amount the amount to deposit
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> deposit(String id, BigDecimal amount);

    /**
     * Withdraws money from a passive product.
     *
     * @param id the product ID
     * @param amount the amount to withdraw
     * @return Mono of PassiveProductDto
     */
    Mono<PassiveProductDto> withdraw(String id, BigDecimal amount);

    /**
     * Transfers money between passive products.
     *
     * @param fromId the source product ID
     * @param toId the destination product ID
     * @param amount the amount to transfer
     * @return Mono of Void
     */
    Mono<Void> transfer(String fromId, String toId, BigDecimal amount);
}
