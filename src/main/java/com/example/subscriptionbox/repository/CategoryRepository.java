package com.example.subscriptionbox.repository;
import com.example.subscriptionbox.model.Category; import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category,Long>{}