package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "관리자 주문")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "배송일별 주문 목록 조회",
            description = "deliveryDate를 생략하면 지금 주문 시 배정되는 배송 그룹(14시 마감 기준)을 조회합니다.")
    @GetMapping
    public List<OrderResponse> findByDeliveryDate(
            @Parameter(description = "배송일 (yyyy-MM-dd)", example = "2026-11-27")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate deliveryDate
    ) {
        return orderService.findByDeliveryDate(deliveryDate);
    }
}