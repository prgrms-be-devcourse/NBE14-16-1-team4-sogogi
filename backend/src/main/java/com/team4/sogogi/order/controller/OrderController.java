package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.dto.OrderCreateRequest;
import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주문")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "내 주문 조회")
    @GetMapping
    public List<OrderResponse> findByEmail(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {
        return orderService.findByEmail(email);
    }

    @Operation(summary = "주문 생성", description = "오후 2시 이후 주문은 다음 날 배송됩니다.")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }
}
