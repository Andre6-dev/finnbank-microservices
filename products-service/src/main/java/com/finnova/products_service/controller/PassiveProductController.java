package com.finnova.products_service.controller;

import com.finnova.products_service.model.dto.BalanceDto;
import com.finnova.products_service.model.dto.CreatePassiveProductRequest;
import com.finnova.products_service.model.dto.PassiveProductDto;
import com.finnova.products_service.model.dto.UpdatePassiveProductRequest;
import com.finnova.products_service.service.PassiveProductService;
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
@RequestMapping("/passive-products")
@RequiredArgsConstructor
@Slf4j
public class PassiveProductController {

    private final PassiveProductService passiveProductService;

    /**
     * Creates a new passive product.
     *
     * @param request the create passive product request
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @PostMapping
    public Mono<ResponseEntity<PassiveProductDto>> createPassiveProduct(
            @Valid @RequestBody CreatePassiveProductRequest request) {
        log.info("POST /passive-products - Creating passive product for customer: {}",
                request.getCustomerId());
        return passiveProductService.createPassiveProduct(request)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .doOnSuccess(r -> log.info("Passive product created successfully"))
                .doOnError(e -> log.error("Error creating passive product: {}", e.getMessage()));
    }

    /**
     * Gets all passive products.
     *
     * @return Flux of PassiveProductDto
     */
    @GetMapping
    public Flux<PassiveProductDto> getAllPassiveProducts() {
        log.info("GET /passive-products - Getting all passive products");
        return passiveProductService.findAll()
                .doOnComplete(() -> log.info("Retrieved all passive products"));
    }

    /**
     * Gets a passive product by ID.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PassiveProductDto>> getPassiveProductById(@PathVariable String id) {
        log.info("GET /passive-products/{} - Getting passive product by ID", id);
        return passiveProductService.findById(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Passive product retrieved successfully: {}", id))
                .doOnError(e -> log.error("Error retrieving passive product: {}", e.getMessage()));
    }

    /**
     * Gets passive products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of PassiveProductDto
     */
    @GetMapping("/customer/{customerId}")
    public Flux<PassiveProductDto> getPassiveProductsByCustomerId(@PathVariable String customerId) {
        log.info("GET /passive-products/customer/{} - Getting passive products by customer", customerId);
        return passiveProductService.findByCustomerId(customerId)
                .doOnComplete(() -> log.info("Retrieved passive products for customer: {}", customerId));
    }

    /**
     * Gets a passive product by account number.
     *
     * @param accountNumber the account number
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @GetMapping("/account/{accountNumber}")
    public Mono<ResponseEntity<PassiveProductDto>> getPassiveProductByAccountNumber(
            @PathVariable String accountNumber) {
        log.info("GET /passive-products/account/{} - Getting passive product by account number",
                accountNumber);
        return passiveProductService.findByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Passive product retrieved by account: {}", accountNumber))
                .doOnError(e -> log.error("Error retrieving passive product: {}", e.getMessage()));
    }

    /**
     * Updates a passive product.
     *
     * @param id the product ID
     * @param request the update passive product request
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<PassiveProductDto>> updatePassiveProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdatePassiveProductRequest request) {
        log.info("PUT /passive-products/{} - Updating passive product", id);
        return passiveProductService.updatePassiveProduct(id, request)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Passive product updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating passive product: {}", e.getMessage()));
    }

    /**
     * Deletes a passive product.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePassiveProduct(@PathVariable String id) {
        log.info("DELETE /passive-products/{} - Deleting passive product", id);
        return passiveProductService.deletePassiveProduct(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(r -> log.info("Passive product deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting passive product: {}", e.getMessage()));
    }

    /**
     * Gets balance of a passive product.
     *
     * @param id the product ID
     * @return Mono of ResponseEntity with BalanceDto
     */
    @GetMapping("/{id}/balance")
    public Mono<ResponseEntity<BalanceDto>> getBalance(@PathVariable String id) {
        log.info("GET /passive-products/{}/balance - Getting balance", id);
        return passiveProductService.getBalance(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Balance retrieved for product: {}", id))
                .doOnError(e -> log.error("Error getting balance: {}", e.getMessage()));
    }

    /**
     * Deposits money into a passive product.
     *
     * @param id the product ID
     * @param request the deposit request containing amount
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @PostMapping("/{id}/deposit")
    public Mono<ResponseEntity<PassiveProductDto>> deposit(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        log.info("POST /passive-products/{}/deposit - Depositing amount: {}", id, amount);
        return passiveProductService.deposit(id, amount)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Deposit successful for product: {}", id))
                .doOnError(e -> log.error("Error depositing: {}", e.getMessage()));
    }

    /**
     * Withdraws money from a passive product.
     *
     * @param id the product ID
     * @param request the withdrawal request containing amount
     * @return Mono of ResponseEntity with PassiveProductDto
     */
    @PostMapping("/{id}/withdraw")
    public Mono<ResponseEntity<PassiveProductDto>> withdraw(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        log.info("POST /passive-products/{}/withdraw - Withdrawing amount: {}", id, amount);
        return passiveProductService.withdraw(id, amount)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Withdrawal successful for product: {}", id))
                .doOnError(e -> log.error("Error withdrawing: {}", e.getMessage()));
    }

    /**
     * Transfers money between passive products.
     *
     * @param fromId the source product ID
     * @param request the transfer request containing toId and amount
     * @return Mono of ResponseEntity
     */
    @PostMapping("/{fromId}/transfer")
    public Mono<ResponseEntity<Map<String, String>>> transfer(
            @PathVariable String fromId,
            @RequestBody Map<String, Object> request) {
        String toId = (String) request.get("toId");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        log.info("POST /passive-products/{}/transfer - Transferring {} to {}", fromId, amount, toId);
        return passiveProductService.transfer(fromId, toId, amount)
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Transfer successful"))))
                .doOnSuccess(r -> log.info("Transfer successful from {} to {}", fromId, toId))
                .doOnError(e -> log.error("Error during transfer: {}", e.getMessage()));
    }
}
