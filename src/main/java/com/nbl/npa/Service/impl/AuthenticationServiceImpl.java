package com.nbl.npa.Service.impl;

import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.Entities.TblConfigurationEntity;
import com.nbl.npa.Model.Repo.ConfigurationRepository;
import com.nbl.npa.Service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ConfigurationRepository configUserRepo;
    private final UserDetailsService userDetailsService;

    @Value("${jwt-secret}")
    private String secretKey;

    private final Long jwtExpiryMs = 3600000L;

    @Getter
    private long lastExpiresIn;

    @Override
    public UserDetails authenticate(String email, String password) {
        TblConfigurationEntity configUser = configUserRepo.findByUserId(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String decryptedPassword = AES256.processCrypto(configUser.getPassword(), Cipher.DECRYPT_MODE);

        if (!decryptedPassword.equals(password)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // You can either return a dummy user or load from your own implementation
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS384)
                .compact();

        lastExpiresIn = jwtExpiryMs / 1000;
        return token;
    }

    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    private String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


