package com.example.subscriptionbox.repository;
import com.example.subscriptionbox.model.Product; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product,Long>{ List<Product> findByActiveTrue(); List<Product> findByCategoryIdAndActiveTrue(Long id); }