package com.example.subscriptionbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","product_id"}))
public class Rating {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private AppUser user;
 @ManyToOne(optional=false) private Product product;
 private int score;
 @Column(length=1000) private String comment;
 private LocalDateTime ratedAt=LocalDateTime.now();
 public Rating() {}
 public Long getId(){return id;} public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
 public Product getProduct(){return product;} public void setProduct(Product v){product=v;}
 public int getScore(){return score;} public void setScore(int v){score=v;}
 public String getComment(){return comment;} public void setComment(String v){comment=v;}
 public LocalDateTime getRatedAt(){return ratedAt;} public void setRatedAt(LocalDateTime v){ratedAt=v;}
}
