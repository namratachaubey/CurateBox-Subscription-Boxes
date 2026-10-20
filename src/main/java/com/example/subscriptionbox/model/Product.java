package com.example.subscriptionbox.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String name;
 @Column(length=1000) private String description;
 @Column(nullable=false, precision=10,scale=2) private BigDecimal price;
 private String tags;
 @ManyToOne(optional=false) private Category category;
 private boolean active=true;
 public Product() {}
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
 public String getTags(){return tags;} public void setTags(String v){tags=v;}
 public Category getCategory(){return category;} public void setCategory(Category v){category=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
