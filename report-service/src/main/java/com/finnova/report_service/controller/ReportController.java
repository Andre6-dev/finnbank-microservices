package com.finnova.report_service.controller;

import com.finnova.report_service.model.dto.CommissionReportDto;
import com.finnova.report_service.model.dto.ConsolidatedReportDto;
import com.finnova.report_service.model.dto.DailyAverageReportDto;
import com.finnova.report_service.model.dto.ProductReportDto;
import com.finnova.report_service.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report generation endpoints")
public class ReportController {

    private final ReportService reportService;

    // ========== CONSOLIDATED REPORT ==========

    @GetMapping("/customer/{customerId}/consolidated")
    @Operation(summary = "Generate consolidated report for a customer (Proyecto III)")
    public Mono<ResponseEntity<ConsolidatedReportDto>> getConsolidatedReport(
            @PathVariable String customerId
    ) {
        log.info("Generating consolidated report for customer: {}", customerId);
        return reportService.getConsolidatedReport(customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error generating consolidated report", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ========== DAILY AVERAGE REPORT ==========

    @GetMapping("/customer/{customerId}/daily-average")
    @Operation(summary = "Generate daily average balance report (Proyecto II)")
    public Mono<ResponseEntity<DailyAverageReportDto>> getDailyAverageReport(
            @PathVariable String customerId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        log.info("Generating daily average report for customer: {} - {}/{}", customerId, month, year);
        return reportService.getDailyAverageBalanceReport(customerId, month, year)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error generating daily average report", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ========== COMMISSION REPORT ==========

    @GetMapping("/commissions")
    @Operation(summary = "Generate commission report for a product (Proyecto II)")
    public Mono<ResponseEntity<CommissionReportDto>> getCommissionReport(
            @RequestParam String productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Generating commission report for product: {} from {} to {}", productId, startDate, endDate);
        return reportService.getCommissionReport(productId, startDate, endDate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error generating commission report", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ========== PRODUCT REPORT ==========

    @GetMapping("/product")
    @Operation(summary = "Generate general product report (Proyecto III)")
    public Mono<ResponseEntity<ProductReportDto>> getProductReport(
            @RequestParam String productType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Generating product report for type: {} from {} to {}", productType, startDate, endDate);
        return reportService.getProductReport(productType, startDate, endDate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error generating product report", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
