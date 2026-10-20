package com.example.subscriptionbox.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
public class MonthlyOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private AppUser user;
 @ManyToOne(optional=false) private SubscriptionPlan subscriptionPlan;
 private LocalDate orderDate=LocalDate.now();
 @Column(nullable=false,precision=10,scale=2) private BigDecimal total;
 @Column(nullable=false) private String status="GENERATED";
 @ManyToMany @JoinTable(name="monthly_order_products", joinColumns=@JoinColumn(name="order_id"), inverseJoinColumns=@JoinColumn(name="product_id"))
 private List<Product> products=new ArrayList<>();
 public MonthlyOrder() {}
 public Long getId(){return id;} public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
 public SubscriptionPlan getSubscriptionPlan(){return subscriptionPlan;} public void setSubscriptionPlan(SubscriptionPlan v){subscriptionPlan=v;}
 public LocalDate getOrderDate(){return orderDate;} public void setOrderDate(LocalDate v){orderDate=v;}
 public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public List<Product> getProducts(){return products;} public void setProducts(List<Product> v){products=v;}
}
