package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderItem;
import com.team4.sogogi.product.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    private static final String EMAIL = "a@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    private Order saveOrder(String email, Product product, int quantity) {
        Order order = Order.builder()
                .email(email).address("서울").postcode("06236")
                .deliveryDate(LocalDate.of(2026, 11, 27))
                .build();
        order.addOrderItem(OrderItem.builder().product(product).quantity(quantity).build());
        em.persist(order);
        em.flush();
        return order;
    }

    @Test
    void 내_주문을_최신순으로_조회() throws Exception {
        Product product = Product.builder().name("A").category("커피콩").price(5000).description("설명").build();
        em.persist(product);
        Order older = saveOrder(EMAIL, product, 1);
        Order newer = saveOrder(EMAIL, product, 2);
        saveOrder("other@example.com", product, 1);

        mockMvc.perform(get("/api/orders").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(newer.getId()))
                .andExpect(jsonPath("$[0].totalPrice").value(10000))
                .andExpect(jsonPath("$[0].items[0].name").value("A"))
                .andExpect(jsonPath("$[1].orderId").value(older.getId()));
    }

    @Test
    void 주문이_없으면_빈_배열() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", "none@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("email: 이메일 형식이 올바르지 않습니다."));
    }

    @Test
    void 이메일이_비었으면_400() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: 이메일은 필수입니다."));
    }

    @Test
    void 이메일이_없으면_400() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }
}
