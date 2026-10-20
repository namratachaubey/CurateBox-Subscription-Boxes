package com.example.subscriptionbox.service;
import com.example.subscriptionbox.model.AppUser; import com.example.subscriptionbox.repository.UserRepository;
import org.springframework.security.core.Authentication; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component;
@Component
public class CurrentUser {
 private final UserRepository users; public CurrentUser(UserRepository users){this.users=users;}
 public AppUser get(){Authentication a=SecurityContextHolder.getContext().getAuthentication();return users.findByEmail(a.getName()).orElseThrow();}
}
