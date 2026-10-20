
package com.example.subscriptionbox.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private AppUser user;

    // Keep the original category field for compatibility
    // with existing parts of the application.
    @ManyToOne
    private Category category;

    // Store all categories selected by the customer.
    @ManyToMany
    @JoinTable(
        name = "user_preference_categories",
        joinColumns = @JoinColumn(name = "preference_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    private BigDecimal monthlyBudget = new BigDecimal("30.00");

    @Column(length = 1000)
    private String interests;

    @Column(length = 1000)
    private String avoidItems;

    public Preference() {}

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser v) {
        user = v;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category v) {
        category = v;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> v) {
        categories = v == null ? new HashSet<>() : new HashSet<>(v);
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal v) {
        monthlyBudget = v;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String v) {
        interests = v;
    }

    public String getAvoidItems() {
        return avoidItems;
    }

    public void setAvoidItems(String v) {
        avoidItems = v;
    }
}
