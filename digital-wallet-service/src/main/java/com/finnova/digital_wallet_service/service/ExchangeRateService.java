package com.finnova.digital_wallet_service.service;

import com.finnova.digital_wallet_service.model.dto.SetExchangeRateRequest;
import com.finnova.digital_wallet_service.model.entity.ExchangeRate;
import com.finnova.digital_wallet_service.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Set new exchange rate (only admin)
     */
    public Mono<ExchangeRate> setExchangeRate(SetExchangeRateRequest request) {
        log.info("Setting new exchange rate: buy={}, sell={}", request.getBuyRate(), request.getSellRate());

        // Deactivate current rate
        return exchangeRateRepository.findFirstByIsActiveTrueOrderByEffectiveDateDesc()
                .flatMap(currentRate -> {
                    currentRate.setIsActive(false);
                    return exchangeRateRepository.save(currentRate);
                })
                .then(Mono.defer(() -> {
                    // Create new rate
                    ExchangeRate newRate = ExchangeRate.builder()
                            .buyRate(request.getBuyRate())
                            .sellRate(request.getSellRate())
                            .effectiveDate(LocalDateTime.now())
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return exchangeRateRepository.save(newRate);
                }))
                .switchIfEmpty(Mono.defer(() -> {
                    // No current rate exists, create first one
                    ExchangeRate newRate = ExchangeRate.builder()
                            .buyRate(request.getBuyRate())
                            .sellRate(request.getSellRate())
                            .effectiveDate(LocalDateTime.now())
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return exchangeRateRepository.save(newRate);
                }));
    }

    /**
     * Get current exchange rate
     */
    public Mono<ExchangeRate> getCurrentRate() {
        return exchangeRateRepository.findFirstByIsActiveTrueOrderByEffectiveDateDesc()
                .switchIfEmpty(Mono.error(new RuntimeException("No exchange rate configured")));
    }
}
