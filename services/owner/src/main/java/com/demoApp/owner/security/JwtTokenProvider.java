package com.demoApp.owner.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:ZGVtb0FwcFNlY3JldEtleTEyMzQ1Njc4OTAxMjM0NTY3ODkw}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationDate;

    // Method to generate JWT token for a given authentication
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Log the token generation process
        log.info("Generating token for user: {}", username);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())  // Sign the JWT with the provided key
                .compact();
    }

    // Private method to create the key for signing the JWT token
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Method to get the username from the token
    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts a specific claim from the JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Private method to extract all claims from the token
    private Claims extractAllClaims(String token) {
        log.debug("Extracting claims from token: {}", token);  // Log claim extraction

        return Jwts.parserBuilder()
                .setSigningKey(key())  // Set the signing key
                .build()  // Build the parser
                .parseClaimsJws(token)  // Parse the JWT and extract claims
                .getBody();  // Get the claims from the parsed JWT
    }

    // Validates if the token is valid based on username and expiration
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsername(token);
        log.info("Validating token for user: {}", username);  // Log validation process

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Checks if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
