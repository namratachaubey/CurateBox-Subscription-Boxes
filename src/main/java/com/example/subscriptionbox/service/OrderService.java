package com.example.subscriptionbox.service;
import com.example.subscriptionbox.model.*; import com.example.subscriptionbox.repository.*;
import org.springframework.stereotype.Service; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@Service
public class OrderService {
 private final SubscriptionRepository subscriptions; private final OrderRepository orders; private final RecommendationService recommendations; private final ProductRepository products;
 public OrderService(SubscriptionRepository s,OrderRepository o,RecommendationService r,ProductRepository p){subscriptions=s;orders=o;recommendations=r;products=p;}
 public MonthlyOrder generate(SubscriptionPlan plan){
  List<Product> picks=recommendations.recommend(plan.getUser()).stream().filter(p->p.getCategory().getId().equals(plan.getCategory().getId())).toList();
  List<Product> chosen=new ArrayList<>(); BigDecimal total=BigDecimal.ZERO;
  for(Product p:picks){if(total.add(p.getPrice()).compareTo(plan.getMonthlyBudget())<=0){chosen.add(p);total=total.add(p.getPrice());}}
  if(chosen.isEmpty()){products.findByCategoryIdAndActiveTrue(plan.getCategory().getId()).stream().filter(p->p.getPrice().compareTo(plan.getMonthlyBudget())<=0).findFirst().ifPresent(p->{chosen.add(p);});if(!chosen.isEmpty())total=chosen.get(0).getPrice();}
  MonthlyOrder order=new MonthlyOrder();order.setUser(plan.getUser());order.setSubscriptionPlan(plan);order.setOrderDate(LocalDate.now());order.setProducts(chosen);order.setTotal(total);return orders.save(order);
 }
 public List<MonthlyOrder> ordersFor(AppUser user){return orders.findByUserIdOrderByOrderDateDesc(user.getId());}
}
