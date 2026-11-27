package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "주문")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "내 주문 조회")
    @GetMapping
    public List<OrderResponse> findByEmail(@RequestParam @NotBlank @Email String email) {
        return orderService.findByEmail(email);
    }
}