package com.yash.hospitalManagement.security;

import com.yash.hospitalManagement.entity.User;
import com.yash.hospitalManagement.entity.type.AuthProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class AuthUtil {
    @Value("${jwt.secretKey}")
    private String secretKey;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId",user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*10))
                .signWith(getSecretKey())
                .compact();

    }

    public String getUsernameFromToken(String token) {
        Claims claims=Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public AuthProviderType getProviderType(String registrationId) {
        return switch(registrationId.toLowerCase()){
            case "google"->AuthProviderType.GOOGLE;
            case "github"->AuthProviderType.GITHUB;
            case "facebook"->AuthProviderType.FACEBOOK;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 Provider: " + registrationId);
        };
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User,String registrationId) {
        String providerId=switch(registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> {
                log.error("Unsupported OAuth2 Provider: {}",registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 Provider: " + registrationId);
            }
        };
        if(providerId==null){
            log.error("Unable to determine providerId for provider: {}",registrationId);
            throw new IllegalStateException("Unable to determine providerId for provider: " + registrationId);
        }
        return providerId;
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User,String registrationId,String providerId) {
        String email=oAuth2User.getAttribute("email");
        if(email!=null && !email.isBlank()){
            return email;
        }
        return switch(registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }
}
