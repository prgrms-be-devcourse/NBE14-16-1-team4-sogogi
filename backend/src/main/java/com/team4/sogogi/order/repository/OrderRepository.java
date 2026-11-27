package com.team4.sogogi.order.repository;

import com.team4.sogogi.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository <Order, Long>{

    List<Order>findByEmailOrderByCreatedAtDesc(String email);
}
