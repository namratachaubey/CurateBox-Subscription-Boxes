package com.example.subscriptionbox.repository;
import com.example.subscriptionbox.model.MonthlyOrder; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<MonthlyOrder,Long>{ List<MonthlyOrder> findByUserIdOrderByOrderDateDesc(Long id); }