package com.example.subscriptionbox.service;
import com.example.subscriptionbox.repository.SubscriptionRepository; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component;
import java.time.LocalDate;
@Component
public class MonthlyOrderScheduler {
 private final SubscriptionRepository subscriptions; private final OrderService orders;
 public MonthlyOrderScheduler(SubscriptionRepository s,OrderService o){subscriptions=s;orders=o;}
 @Scheduled(cron="0 0 8 * * *")
 public void createDueOrders(){for(var plan:subscriptions.findByActiveTrueAndNextOrderDateLessThanEqual(LocalDate.now())){orders.generate(plan);plan.setNextOrderDate(plan.getNextOrderDate().plusMonths(1));subscriptions.save(plan);}}
}
