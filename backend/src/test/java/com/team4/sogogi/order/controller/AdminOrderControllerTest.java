package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.domain.DeliveryPolicy;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private DeliveryPolicy deliveryPolicy;

    private Product saveProduct(String name, int price) {
        Product product = Product.builder()
                .name(name).category("커피콩").price(price).description("설명")
                .build();
        em.persist(product);
        return product;
    }

    private Order saveOrder(String email, LocalDate deliveryDate, Product product) {
        Order order = Order.builder()
                .email(email).address("서울").postcode("06236")
                .deliveryDate(deliveryDate)
                .build();
        order.addOrderItem(OrderItem.builder().product(product).quantity(1).build());
        em.persist(order);
        em.flush();
        return order;
    }

    private static String orderBody(String email, long productId, int quantity) {
        return """
                {
                  "email": "%s",
                  "address": "서울시 강남구",
                  "postcode": "06236",
                  "items": [{ "productId": %d, "quantity": %d }]
                }
                """.formatted(email, productId, quantity);
    }

    @Test
    void 선택한_배송일의_주문만_조회된다() throws Exception {
        Product product = saveProduct("A", 5000);
        Order target = saveOrder("a@example.com", LocalDate.of(2026, 11, 27), product);
        saveOrder("b@example.com", LocalDate.of(2026, 11, 28), product);

        mockMvc.perform(get("/api/admin/orders").param("deliveryDate", "2026-11-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(target.getId()))
                .andExpect(jsonPath("$[0].deliveryDate").value("2026-11-27"));
    }

    @Test
    void 배송일을_생략하면_현재_배송_그룹을_조회한다() throws Exception {
        Product product = saveProduct("A", 5000);
        LocalDate currentGroup = deliveryPolicy.getDeliveryDateKorea();
        Order current = saveOrder("a@example.com", currentGroup, product);
        saveOrder("b@example.com", currentGroup.minusDays(1), product);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(current.getId()));
    }

    @Test
    void 같은_이메일_주문은_병합되어_1건으로_보인다() throws Exception {
        Product a = saveProduct("A", 5000);
        Product b = saveProduct("B", 3000);
        em.flush();

        // 같은 이메일로 두 번 주문 (같은 배송 그룹)
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("a@example.com", a.getId(), 1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("a@example.com", b.getId(), 2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items.length()").value(2))
                .andExpect(jsonPath("$[0].totalPrice").value(5000 + 3000 * 2));
    }

    @Test
    void 배송일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("deliveryDate", "2026-13-40"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }
}