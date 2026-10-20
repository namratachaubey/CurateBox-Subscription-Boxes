package com.example.subscriptionbox.security;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserDetailsService users;
 public JwtAuthFilter(JwtService jwt,UserDetailsService users){this.jwt=jwt;this.users=users;}
 @Override
 protected boolean shouldNotFilter(HttpServletRequest request) {
     return request.getServletPath().startsWith("/api/auth/");
 }
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String header=req.getHeader("Authorization");
  if(header!=null&&header.startsWith("Bearer ")&&SecurityContextHolder.getContext().getAuthentication()==null){
   try{String token=header.substring(7);String email=jwt.extractEmail(token);UserDetails u=users.loadUserByUsername(email);
    if(jwt.valid(token,email)){var auth=new UsernamePasswordAuthenticationToken(u,null,u.getAuthorities());auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));SecurityContextHolder.getContext().setAuthentication(auth);}
   }catch(Exception ignored){}
  } chain.doFilter(req,res);
 }
}
