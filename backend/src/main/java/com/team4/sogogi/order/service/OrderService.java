package com.team4.sogogi.order.service;

import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> findByEmail(String email) {
        return orderRepository.findByEmailOrderByCreatedAtDesc(email).stream()
                .map(OrderResponse::from)
                .toList();
    }
}
