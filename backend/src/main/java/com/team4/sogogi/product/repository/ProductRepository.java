package com.team4.sogogi.product.repository;

import com.team4.sogogi.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
