package com.finnova.auth_service.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.finnova.auth_service.model.entity.User;
import com.finnova.auth_service.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * Gets the algorithm for signing/verifying tokens.
     *
     * @return the HMAC256 algorithm
     */
    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    @Override
    public Mono<String> generateToken(User user) {
        return Mono.fromCallable(() -> {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            String token = JWT.create()
                    .withSubject(user.getUsername())
                    .withIssuer(issuer)
                    .withIssuedAt(now)
                    .withExpiresAt(expiryDate)
                    .withClaim("userId", user.getId())
                    .withClaim("email", user.getEmail())
                    .withArrayClaim("roles", user.getRoles().toArray(new String[0]))
                    .sign(getAlgorithm());

            log.debug("Generated JWT token for user: {}", user.getUsername());
            return token;
        });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                JWTVerifier verifier = JWT.require(getAlgorithm())
                        .withIssuer(issuer)
                        .build();

                DecodedJWT decodedJWT = verifier.verify(token);

                // Check if token has expired
                if (decodedJWT.getExpiresAt().before(new Date())) {
                    log.warn("Token has expired");
                    return false;
                }

                log.debug("Token validated successfully");
                return true;

            } catch (JWTVerificationException e) {
                log.error("Token validation failed: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() -> {
            try {
                DecodedJWT decodedJWT = JWT.decode(token);
                return decodedJWT.getSubject();
            } catch (Exception e) {
                log.error("Error extracting username from token: {}", e.getMessage());
                return null;
            }
        });
    }

    @Override
    public Long getExpirationTime() {
        return expiration;
    }
}
