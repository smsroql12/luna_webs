package com.example.luna.schedule;

import com.example.luna.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {
    private final OrderRepository orderRepository;

    // 매일 새벽 3시에 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0 * * *")
    public void autoUpdateOrders() {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        int canceled = orderRepository.cancelUnpaidOrders(twoWeeksAgo);
        int completed = orderRepository.completeShippedOrders(twoWeeksAgo);

        log.info("자동 주문 상태 업데이트 완료 - 취소 {}건, 수령완료 {}건", canceled, completed);
    }
}
