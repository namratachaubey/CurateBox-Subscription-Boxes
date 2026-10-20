package com.example.subscriptionbox.controller;
import com.example.subscriptionbox.model.*; import com.example.subscriptionbox.repository.UserRepository; import com.example.subscriptionbox.security.JwtService;
import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus; import org.springframework.security.authentication.AuthenticationManager; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users;private final PasswordEncoder encoder;private final AuthenticationManager auth;private final JwtService jwt;
 public AuthController(UserRepository u,PasswordEncoder e,AuthenticationManager a,JwtService j){users=u;encoder=e;auth=a;jwt=j;}
 public record RegisterRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank String password){}
 public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
 @PostMapping("/register") public Map<String,Object> register(@RequestBody RegisterRequest req){
  if(users.existsByEmail(req.email()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
  AppUser u=new AppUser();u.setName(req.name());u.setEmail(req.email().toLowerCase());u.setPassword(encoder.encode(req.password()));u.setRole(Role.CUSTOMER);users.save(u);
  return Map.of("token",jwt.generate(u.getEmail()),"name",u.getName(),"email",u.getEmail(),"role",u.getRole().name());
 }
 @PostMapping("/login") public Map<String,Object> login(@RequestBody LoginRequest req){
  auth.authenticate(new UsernamePasswordAuthenticationToken(req.email().toLowerCase(),req.password()));
  AppUser u=users.findByEmail(req.email().toLowerCase()).orElseThrow();return Map.of("token",jwt.generate(u.getEmail()),"name",u.getName(),"email",u.getEmail(),"role",u.getRole().name());
 }
}
