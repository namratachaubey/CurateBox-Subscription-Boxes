package com.example.subscriptionbox.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
public class SubscriptionPlan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private AppUser user;
 @ManyToOne(optional=false) private Category category;
 @Column(nullable=false) private String planName;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal monthlyBudget;
 private boolean active=true;
 private LocalDate nextOrderDate;
 public SubscriptionPlan() {}
 public Long getId(){return id;} public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
 public Category getCategory(){return category;} public void setCategory(Category v){category=v;}
 public String getPlanName(){return planName;} public void setPlanName(String v){planName=v;}
 public BigDecimal getMonthlyBudget(){return monthlyBudget;} public void setMonthlyBudget(BigDecimal v){monthlyBudget=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
 public LocalDate getNextOrderDate(){return nextOrderDate;} public void setNextOrderDate(LocalDate v){nextOrderDate=v;}
}
