package com.finnova.products_service.controller;

import com.finnova.products_service.model.dto.ActiveProductDto;
import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreateActiveProductRequest;
import com.finnova.products_service.model.dto.UpdateActiveProductRequest;
import com.finnova.products_service.service.ActiveProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/active-products")
@RequiredArgsConstructor
@Slf4j
public class ActiveProductController {

    private final ActiveProductService activeProductService;

    /**
     * Creates a new active product.
     *
     * @param request the create active product request
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @PostMapping
    public Mono<ResponseEntity<ActiveProductDto>> createActiveProduct(
            @Valid @RequestBody CreateActiveProductRequest request) {
        log.info("POST /active-products - Creating active product for customer: {}",
                request.getCustomerId());
        return activeProductService.createActiveProduct(request)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .doOnSuccess(r -> log.info("Active product created successfully"))
                .doOnError(e -> log.error("Error creating active product: {}", e.getMessage()));
    }

    /**
     * Gets all active products.
     *
     * @return Flux of ActiveProductDto
     */
    @GetMapping
    public Flux<ActiveProductDto> getAllActiveProducts() {
        log.info("GET /active-products - Getting all active products");
        return activeProductService.findAll()
                .doOnComplete(() -> log.info("Retrieved all active products"));
    }

    /**
     * Gets an active product by ID.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ActiveProductDto>> getActiveProductById(@PathVariable String id) {
        log.info("GET /active-products/{} - Getting active product by ID", id);
        return activeProductService.findById(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Active product retrieved successfully: {}", id))
                .doOnError(e -> log.error("Error retrieving active product: {}", e.getMessage()));
    }

    /**
     * Gets active products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of ActiveProductDto
     */
    @GetMapping("/customer/{customerId}")
    public Flux<ActiveProductDto> getActiveProductsByCustomerId(@PathVariable String customerId) {
        log.info("GET /active-products/customer/{} - Getting active products by customer", customerId);
        return activeProductService.findByCustomerId(customerId)
                .doOnComplete(() -> log.info("Retrieved active products for customer: {}", customerId));
    }

    /**
     * Gets an active product by credit number.
     *
     * @param creditNumber the credit number
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @GetMapping("/credit/{creditNumber}")
    public Mono<ResponseEntity<ActiveProductDto>> getActiveProductByCreditNumber(
            @PathVariable String creditNumber) {
        log.info("GET /active-products/credit/{} - Getting active product by credit number",
                creditNumber);
        return activeProductService.findByCreditNumber(creditNumber)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Active product retrieved by credit: {}", creditNumber))
                .doOnError(e -> log.error("Error retrieving active product: {}", e.getMessage()));
    }

    /**
     * Updates an active product.
     *
     * @param id the product ID
     * @param request the update active product request
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ActiveProductDto>> updateActiveProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateActiveProductRequest request) {
        log.info("PUT /active-products/{} - Updating active product", id);
        return activeProductService.updateActiveProduct(id, request)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Active product updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating active product: {}", e.getMessage()));
    }

    /**
     * Deletes an active product.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteActiveProduct(@PathVariable String id) {
        log.info("DELETE /active-products/{} - Deleting active product", id);
        return activeProductService.deleteActiveProduct(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(r -> log.info("Active product deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting active product: {}", e.getMessage()));
    }

    /**
     * Gets available credit of an active product.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity with BalanceDto
     */
    @GetMapping("/{id}/available-credit")
    public Mono<ResponseEntity<BalanceDto>> getAvailableCredit(@PathVariable String id) {
        log.info("GET /active-products/{}/available-credit - Getting available credit", id);
        return activeProductService.getAvailableCredit(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Available credit retrieved for product: {}", id))
                .doOnError(e -> log.error("Error getting available credit: {}", e.getMessage()));
    }

    /**
     * Makes a charge to the credit.
     *
     * @param id the product ID
     * @param request the charge request containing amount
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @PostMapping("/{id}/charge")
    public Mono<ResponseEntity<ActiveProductDto>> makeCharge(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        log.info("POST /active-products/{}/charge - Making charge: {}", id, amount);
        return activeProductService.makeCharge(id, amount)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Charge successful for product: {}", id))
                .doOnError(e -> log.error("Error making charge: {}", e.getMessage()));
    }

    /**
     * Makes a payment to the credit.
     *
     * @param id the product ID
     * @param request the payment request containing amount
     * @return Mono of ResponseEntity with ActiveProductDto
     */
    @PostMapping("/{id}/payment")
    public Mono<ResponseEntity<ActiveProductDto>> makePayment(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        log.info("POST /active-products/{}/payment - Making payment: {}", id, amount);
        return activeProductService.makePayment(id, amount)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Payment successful for product: {}", id))
                .doOnError(e -> log.error("Error making payment: {}", e.getMessage()));
    }

    /**
     * Checks if customer has overdue debt.
     *
     * @param customerId the customer ID
     * @return Mono of ResponseEntity with boolean result
     */
    @GetMapping("/customer/{customerId}/has-overdue-debt")
    public Mono<ResponseEntity<Map<String, Boolean>>> hasOverdueDebt(@PathVariable String customerId) {
        log.info("GET /active-products/customer/{}/has-overdue-debt - Checking overdue debt", customerId);
        return activeProductService.hasOverdueDebt(customerId)
                .map(hasDebt -> ResponseEntity.ok(Map.of("hasOverdueDebt", hasDebt)))
                .doOnSuccess(r -> log.info("Overdue debt check completed for customer: {}", customerId))
                .doOnError(e -> log.error("Error checking overdue debt: {}", e.getMessage()));
    }
}
