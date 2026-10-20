
package com.example.subscriptionbox.controller;

import com.example.subscriptionbox.model.*;
import com.example.subscriptionbox.repository.*;

import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final UserRepository users;
    private final SubscriptionRepository subscriptions;
    private final OrderRepository orders;
    private final RatingRepository ratings;
    private final PreferenceRepository preferences;

    public AdminController(
            CategoryRepository categories,
            ProductRepository products,
            UserRepository users,
            SubscriptionRepository subscriptions,
            OrderRepository orders,
            RatingRepository ratings,
            PreferenceRepository preferences) {

        this.categories = categories;
        this.products = products;
        this.users = users;
        this.subscriptions = subscriptions;
        this.orders = orders;
        this.ratings = ratings;
        this.preferences = preferences;
    }

    public record CategoryRequest(
            @NotBlank String name,
            String description) {}

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            String tags,
            @NotNull Long categoryId) {}

    public record Overview(
            long totalCustomers,
            long activeSubscriptions,
            long totalOrders,
            BigDecimal totalOrderValue,
            double averageRating,
            long totalRatings) {}

    public record CustomerInfo(
            Long id,
            String name,
            String email,
            LocalDateTime registeredAt,
            String interests,
            String avoidItems,
            BigDecimal preferenceBudget,
            String preferenceCategory,
            long activeSubscriptions) {}

    public record SubscriptionInfo(
            Long id,
            Long customerId,
            String customerName,
            String customerEmail,
            String planName,
            String category,
            BigDecimal monthlyBudget,
            boolean active,
            java.time.LocalDate nextOrderDate) {}

    public record OrderInfo(
            Long id,
            Long customerId,
            String customerName,
            String planName,
            java.time.LocalDate orderDate,
            BigDecimal total,
            String status,
            List<String> products) {}

    public record RatingInfo(
            Long id,
            Long customerId,
            String customerName,
            String productName,
            int score,
            String comment,
            LocalDateTime ratedAt) {}

    @GetMapping("/overview")
    public Overview getOverview() {

        long customerCount = users.findAll().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .count();

        long activeCount = subscriptions.findAll().stream()
                .filter(SubscriptionPlan::isActive)
                .count();

        List<MonthlyOrder> allOrders = orders.findAll();

        BigDecimal orderValue = allOrders.stream()
                .map(MonthlyOrder::getTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Rating> allRatings = ratings.findAll();

        double average = allRatings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        return new Overview(
                customerCount,
                activeCount,
                allOrders.size(),
                orderValue,
                average,
                allRatings.size());
    }

    @GetMapping("/customers")
    public List<CustomerInfo> getCustomers() {

        Map<Long, Preference> preferenceMap = preferences.findAll()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        p -> p,
                        (first, second) -> first));

        Map<Long, Long> activeCounts = subscriptions.findAll()
                .stream()
                .filter(SubscriptionPlan::isActive)
                .collect(Collectors.groupingBy(
                        s -> s.getUser().getId(),
                        Collectors.counting()));

        return users.findAll().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .map(u -> {
                    Preference p = preferenceMap.get(u.getId());

                    return new CustomerInfo(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getCreatedAt(),
                            p == null ? null : p.getInterests(),
                            p == null ? null : p.getAvoidItems(),
                            p == null ? null : p.getMonthlyBudget(),
                            p == null ? null : p.getCategory().getName(),
                            activeCounts.getOrDefault(u.getId(), 0L));
                })
                .toList();
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionInfo> getSubscriptions() {

        return subscriptions.findAll().stream()
                .map(s -> new SubscriptionInfo(
                        s.getId(),
                        s.getUser().getId(),
                        s.getUser().getName(),
                        s.getUser().getEmail(),
                        s.getPlanName(),
                        s.getCategory().getName(),
                        s.getMonthlyBudget(),
                        s.isActive(),
                        s.getNextOrderDate()))
                .toList();
    }

    @GetMapping("/orders")
    public List<OrderInfo> getOrders() {

        return orders.findAll().stream()
                .map(o -> new OrderInfo(
                        o.getId(),
                        o.getUser().getId(),
                        o.getUser().getName(),
                        o.getSubscriptionPlan().getPlanName(),
                        o.getOrderDate(),
                        o.getTotal(),
                        o.getStatus(),
                        o.getProducts().stream()
                                .map(Product::getName)
                                .toList()))
                .toList();
    }

    @GetMapping("/ratings")
    public List<RatingInfo> getRatings() {

        return ratings.findAll().stream()
                .map(r -> new RatingInfo(
                        r.getId(),
                        r.getUser().getId(),
                        r.getUser().getName(),
                        r.getProduct().getName(),
                        r.getScore(),
                        r.getComment(),
                        r.getRatedAt()))
                .toList();
    }

    @PostMapping("/categories")
    public Category addCategory(@RequestBody CategoryRequest r) {
        return categories.save(new Category(r.name(), r.description()));
    }

    @PostMapping("/products")
    public Product addProduct(@RequestBody ProductRequest r) {
        Product p = new Product();
        p.setName(r.name());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setTags(r.tags());
        p.setCategory(
                categories.findById(r.categoryId()).orElseThrow());
        return products.save(p);
    }

    @PutMapping("/products/{id}/deactivate")
    public Product deactivate(@PathVariable Long id) {
        Product p = products.findById(id).orElseThrow();
        p.setActive(false);
        return products.save(p);
    }
}
