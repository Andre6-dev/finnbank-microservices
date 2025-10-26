package com.finnova.gateway_server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT Utility class for token operations.
 * Provides methods to validate and extract information from JWT tokens.
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer:banking-system}")
    private String issuer;

    /**
     * Gets the algorithm for signing/verifying tokens.
     *
     * @return the HMAC256 algorithm with the secret
     */
    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    /**
     * Creates a JWT verifier with the configured algorithm and issuer.
     *
     * @return the JWT verifier
     */
    private JWTVerifier getVerifier() {
        return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build();
    }

    /**
     * Decodes and verifies the JWT token.
     *
     * @param token the JWT token
     * @return the decoded JWT
     * @throws JWTVerificationException if token is invalid
     */
    private DecodedJWT decodeToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token);
    }

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getExpiresAt();
        } catch (JWTVerificationException e) {
            log.error("Error extracting expiration from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a custom claim from the token.
     *
     * @param token the JWT token
     * @param claimName the name of the claim
     * @return the claim value as String, or null if not found
     */
    public String extractClaim(String token, String claimName) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getClaim(claimName).asString();
        } catch (JWTVerificationException e) {
            log.error("Error extracting claim '{}' from token: {}", claimName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the issuer from the token.
     *
     * @param token the JWT token
     * @return the issuer
     */
    public String extractIssuer(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getIssuer();
        } catch (Exception e) {
            log.error("Error extracting issuer from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the token ID from the token.
     *
     * @param token the JWT token
     * @return the token ID
     */
    public String extractTokenId(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getId();
        } catch (JWTVerificationException e) {
            log.error("Error extracting token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validates the JWT token.
     * Performs verification of signature, issuer, and expiration.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);

            // Additional validations
            if (decodedJWT.getSubject() == null || decodedJWT.getSubject().isEmpty()) {
                log.warn("Token has no subject (username)");
                return false;
            }

            if (isTokenExpired(token)) {
                log.warn("Token is expired");
                return false;
            }

            log.debug("Token validated successfully for user: {}", decodedJWT.getSubject());
            return true;

        } catch (JWTVerificationException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates the token and returns the decoded JWT if valid.
     *
     * @param token the JWT token
     * @return the decoded JWT if valid, null otherwise
     */
    public DecodedJWT validateAndDecode(String token) {
        try {
            return decodeToken(token);
        } catch (JWTVerificationException e) {
            log.error("Token validation and decode failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts all user roles from the token.
     * Assumes roles are stored in a claim named "roles" as an array.
     *
     * @param token the JWT token
     * @return array of roles, or empty array if not found
     */
    public String[] extractRoles(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getClaim("roles").asArray(String.class);
        } catch (JWTVerificationException e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            return new String[0];
        }
    }

    /**
     * Checks if the token has a specific role.
     *
     * @param token the JWT token
     * @param role the role to check
     * @return true if token has the role, false otherwise
     */
    public Boolean hasRole(String token, String role) {
        try {
            String[] roles = extractRoles(token);
            for (String r : roles) {
                if (r.equalsIgnoreCase(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the remaining time until token expiration in seconds.
     *
     * @param token the JWT token
     * @return remaining seconds, or -1 if expired or invalid
     */
    public long getRemainingValidityInSeconds(String token) {
        try {
            Date expiration = extractExpiration(token);
            if (expiration == null) {
                return -1;
            }
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return remainingMs > 0 ? remainingMs / 1000 : -1;
        } catch (Exception e) {
            log.error("Error getting remaining validity: {}", e.getMessage());
            return -1;
        }
    }
}
