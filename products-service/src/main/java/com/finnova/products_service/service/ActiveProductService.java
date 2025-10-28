package com.finnova.products_service.service;

import com.finnova.products_service.model.dto.ActiveProductDto;
import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreateActiveProductRequest;
import com.finnova.products_service.model.dto.UpdateActiveProductRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ActiveProductService {

    /**
     * Creates a new active product.
     *
     * @param request the create active product request
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> createActiveProduct(CreateActiveProductRequest request);

    /**
     * Updates an active product.
     *
     * @param id the product ID
     * @param request the update active product request
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> updateActiveProduct(String id, UpdateActiveProductRequest request);

    /**
     * Deletes an active product.
     *
     * @param id the product ID
     * @return Mono of Void
     */
    Mono<Void> deleteActiveProduct(String id);

    /**
     * Finds an active product by ID.
     *
     * @param id the product ID
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> findById(String id);

    /**
     * Finds all active products.
     *
     * @return Flux of ActiveProductDto
     */
    Flux<ActiveProductDto> findAll();

    /**
     * Finds active products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of ActiveProductDto
     */
    Flux<ActiveProductDto> findByCustomerId(String customerId);

    /**
     * Finds an active product by credit number.
     *
     * @param creditNumber the credit number
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> findByCreditNumber(String creditNumber);

    /**
     * Gets available credit of an active product.
     *
     * @param id the product ID
     * @return Mono of BalanceDto
     */
    Mono<BalanceDto> getAvailableCredit(String id);

    /**
     * Makes a charge to the credit.
     *
     * @param id the product ID
     * @param amount the amount to charge
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> makeCharge(String id, BigDecimal amount);

    /**
     * Makes a payment to the credit.
     *
     * @param id the product ID
     * @param amount the amount to pay
     * @return Mono of ActiveProductDto
     */
    Mono<ActiveProductDto> makePayment(String id, BigDecimal amount);

    /**
     * Checks if customer has overdue debt.
     *
     * @param customerId the customer ID
     * @return Mono of Boolean
     */
    Mono<Boolean> hasOverdueDebt(String customerId);
}
