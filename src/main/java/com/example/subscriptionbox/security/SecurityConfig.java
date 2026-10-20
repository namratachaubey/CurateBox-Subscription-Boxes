package com.example.subscriptionbox.security;
import org.springframework.context.annotation.*; import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; import java.util.List;
@Configuration
public class SecurityConfig {
 private final JwtAuthFilter jwtFilter;
 public SecurityConfig(JwtAuthFilter jwtFilter){this.jwtFilter=jwtFilter;}
 @Bean SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
  http.csrf(c->c.disable()).cors(c->c.configurationSource(req->{var cfg=new CorsConfiguration();cfg.setAllowedOrigins(List.of("http://localhost:5173"));cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));cfg.setAllowedHeaders(List.of("*"));return cfg;}))
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a
		    .requestMatchers("/api/auth/**", "/api/categories", "/api/products", "/error").permitAll()
		    .requestMatchers("/api/admin/**").hasRole("ADMIN")
		    .anyRequest().authenticated()
		)
   .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
}
