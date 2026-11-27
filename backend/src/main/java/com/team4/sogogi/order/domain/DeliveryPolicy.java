package com.team4.sogogi.order.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 주문 시각으로 배송일(배송 그룹)을 계산한다.
 * - 당일 14:00:00 이전 주문 → 당일 배송
 * - 당일 14:00:00 포함 이후 주문 → 다음 날 배송
 * DB에 의존하지 않는 순수 로직이라 단위 테스트로 경계값을 검증한다.
 */
@Component
public class DeliveryPolicy {
    private static final LocalTime CUTOFF_TIME = LocalTime.of(14, 0);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");   //서버 시간대와 상관없이 한국 시각 기준

    //서비스 계층에서 호출될 메소드, 한국 기준 주문시간으로 getDeliveryDate 메소드 호출
    public LocalDate getDeliveryDateKorea() {
        return getDeliveryDate(LocalDateTime.now(ZONE));
    }

    //주문한 시간(LocalDateTime 타입)을 입력받아 배송날짜(LocalDate 타입)를 반환
    public LocalDate getDeliveryDate(LocalDateTime orderedAt) {
        LocalDate orderDate = orderedAt.toLocalDate();          //주문 날짜

        if (orderedAt.toLocalTime().isBefore(CUTOFF_TIME)) {    //마감시간 이전
            return orderDate;                                   //당일 배송
        }                                                       //마감시간 이후
        return orderDate.plusDays(1);                           //다음날 배송
    }
}