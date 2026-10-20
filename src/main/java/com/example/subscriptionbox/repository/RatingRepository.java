package com.example.subscriptionbox.repository;
import com.example.subscriptionbox.model.Rating; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface RatingRepository extends JpaRepository<Rating,Long>{ List<Rating> findByUserId(Long id); Optional<Rating> findByUserIdAndProductId(Long uid,Long pid); }