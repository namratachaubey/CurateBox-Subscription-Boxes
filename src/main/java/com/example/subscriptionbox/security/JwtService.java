package com.example.subscriptionbox.security;
import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets; import java.util.Date; import javax.crypto.SecretKey;
@Service
public class JwtService {
 @Value("${app.jwt.secret}") private String secret;
 @Value("${app.jwt.expiration-ms}") private long expiration;
 private SecretKey key(){return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
 public String generate(String email){long now=System.currentTimeMillis();return Jwts.builder().subject(email).issuedAt(new Date(now)).expiration(new Date(now+expiration)).signWith(key()).compact();}
 public String extractEmail(String token){return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();}
 public boolean valid(String token,String email){try{return extractEmail(token).equals(email);}catch(Exception e){return false;}}
}
