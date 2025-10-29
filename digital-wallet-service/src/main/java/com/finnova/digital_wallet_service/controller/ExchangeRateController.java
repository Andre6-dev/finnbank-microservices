package com.finnova.digital_wallet_service.controller;

import com.finnova.digital_wallet_service.model.dto.SetExchangeRateRequest;
import com.finnova.digital_wallet_service.model.entity.ExchangeRate;
import com.finnova.digital_wallet_service.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/bootcoin/exchange-rates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exchange Rates", description = "BootCoin exchange rate management")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    @Operation(summary = "Set exchange rate (Admin only)")
    public Mono<ResponseEntity<ExchangeRate>> setExchangeRate(@Valid @RequestBody SetExchangeRateRequest request) {
        return exchangeRateService.setExchangeRate(request)
                .map(rate -> ResponseEntity.status(HttpStatus.CREATED).body(rate));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current exchange rate")
    public Mono<ResponseEntity<ExchangeRate>> getCurrentRate() {
        return exchangeRateService.getCurrentRate()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
