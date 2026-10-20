
package com.example.subscriptionbox.service;

import com.example.subscriptionbox.model.*;
import com.example.subscriptionbox.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository products;
    private final PreferenceRepository preferences;
    private final RatingRepository ratings;

    public RecommendationService(
            ProductRepository p,
            PreferenceRepository pref,
            RatingRepository r) {
        products = p;
        preferences = pref;
        ratings = r;
    }

    @Transactional(readOnly = true)
    public List<Product> recommend(AppUser user) {

        Preference pref = preferences.findByUserId(user.getId())
                .orElse(null);

        if (pref == null) {
            return products.findByActiveTrue()
                    .stream()
                    .limit(10)
                    .toList();
        }

        // Get all selected categories.
        Set<Category> selectedCategories = pref.getCategories();

        // Support preferences created before multiple categories
        // were introduced.
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            if (pref.getCategory() != null) {
                selectedCategories = Set.of(pref.getCategory());
            } else {
                return products.findByActiveTrue()
                        .stream()
                        .limit(10)
                        .toList();
            }
        }

        // Collect active products from every selected category.
        List<Product> categoryProducts = selectedCategories.stream()
                .flatMap(category ->
                        products.findByCategoryIdAndActiveTrue(
                                category.getId()
                        ).stream()
                )
                .collect(Collectors.toMap(
                        Product::getId,
                        product -> product,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .toList();

        List<Rating> history = ratings.findByUserId(user.getId());

        Map<Long, Integer> scores = new HashMap<>();

        history.forEach(r ->
                scores.put(r.getProduct().getId(), r.getScore())
        );

        String interests = Optional.ofNullable(pref.getInterests())
                .orElse("")
                .toLowerCase();

        String avoid = Optional.ofNullable(pref.getAvoidItems())
                .orElse("")
                .toLowerCase();

        return categoryProducts.stream()

                // Only show products within the monthly budget.
                .filter(p ->
                        p.getPrice().compareTo(pref.getMonthlyBudget()) <= 0
                )

                // Exclude products matching avoided items.
                .filter(p -> {
                    String productDetails = (
                            p.getName() + " " +
                            p.getDescription() + " " +
                            p.getTags()
                    ).toLowerCase();

                    return avoid.isBlank()
                            || !productDetails.contains(avoid);
                })

                // Prioritize higher-rated products and interest matches.
                .sorted(
                        Comparator.<Product>comparingInt(
                                p -> scores.getOrDefault(p.getId(), 3)
                        ).reversed()
                        .thenComparingInt(
                                p -> matches(p, interests) ? 0 : 1
                        )
                        .thenComparing(Product::getPrice)
                )

                // Return matching products from all selected categories.
                .toList();
    }

    private boolean matches(Product p, String interests) {

        if (interests.isBlank()) {
            return false;
        }

        String details = (
                p.getName() + " " +
                p.getDescription() + " " +
                p.getTags()
        ).toLowerCase();

        return Arrays.stream(interests.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .anyMatch(details::contains);
    }
}
