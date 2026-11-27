package com.team4.sogogi.product.controller;

import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderItem;
import com.team4.sogogi.product.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    private static final String VALID_BODY = """
            {"name": "Columbia Nariñó", "category": "커피콩", "price": 5000, "description": "산미와 단맛"}
            """;

    private static final long NOT_EXISTING_ID = Long.MAX_VALUE;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    private Product saveProduct(String name, int price) {
        Product product = Product.builder().name(name).category("커피콩").price(price).description("설명").build();
        em.persist(product);
        em.flush();
        return product;
    }

    @Test
    void 상품_등록시_201과_Location() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Columbia Nariñó"))
                .andExpect(jsonPath("$.price").value(5000))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void 상품명이_비었거나_가격이_0이하면_400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "category": "커피콩", "price": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void 가격이_없으면_필수_메시지로_400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "category": "커피콩"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("price: 가격은 필수입니다."));
    }

    @Test
    void 가격이_소수면_400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "category": "커피콩", "price": 5000.9}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void 상품_목록_조회_200() throws Exception {
        saveProduct("A", 5000);
        saveProduct("B", 6000);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void 상품_단건_조회_200() throws Exception {
        Product product = saveProduct("A", 5000);

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("A"));
    }

    @Test
    void 없는_상품_조회시_404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", NOT_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다. id=" + NOT_EXISTING_ID));
    }

    @Test
    void 상품_수정_200() throws Exception {
        Product product = saveProduct("A", 5000);

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Columbia Nariñó"))
                .andExpect(jsonPath("$.price").value(5000))
                .andExpect(jsonPath("$.description").value("산미와 단맛"));
    }

    @Test
    void 없는_상품_수정시_404() throws Exception {
        mockMvc.perform(put("/api/products/{id}", NOT_EXISTING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void 상품_수정시_잘못된_입력이면_400() throws Exception {
        Product product = saveProduct("A", 5000);

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "category": "커피콩", "price": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("price: 가격은 0보다 커야 합니다."));
    }

    @Test
    void 상품_삭제_204_후_조회시_404() throws Exception {
        Product product = saveProduct("A", 5000);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 없는_상품_삭제시_404() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", NOT_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void 주문_이력이_있는_상품_삭제시_409() throws Exception {
        Product product = saveProduct("A", 5000);
        Order order = Order.builder()
                .email("a@example.com").address("서울").postcode("06236")
                .deliveryDate(LocalDate.of(2026, 11, 27))
                .build();
        order.addOrderItem(OrderItem.builder().product(product).quantity(1).build());
        em.persist(order);
        em.flush();

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_HAS_ORDERS"));
    }

    @Test
    void id가_숫자가_아니면_400() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }
}
