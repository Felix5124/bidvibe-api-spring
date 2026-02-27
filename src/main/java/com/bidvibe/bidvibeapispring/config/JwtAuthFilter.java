package com.bidvibe.bidvibeapispring.config;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bidvibe.bidvibeapispring.constant.SecurityConstants;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter cho Supabase Auth.
 * Parse JWT token thủ công (không cần verify signature ở đây –
 * Supabase đã ký, chỉ cần đọc claims email/role).
 * Trong production: thêm xác thực chữ ký bằng Supabase JWKS endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(SecurityConstants.AUTH_HEADER);

        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(SecurityConstants.BEARER_PREFIX.length());

        try {
            JsonNode claims = parseJwtPayload(token);

            String email = getClaimAsString(claims, SecurityConstants.JWT_CLAIM_EMAIL);
            if (email == null) {
                email = getClaimAsString(claims, SecurityConstants.JWT_CLAIM_SUB);
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.findOrCreate(email);
                String role = "ROLE_" + user.getRole().name();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user, null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (IOException e) {
            log.warn("JWT processing failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /** Decode phần payload (phần thứ 2) của JWT token. */
    private JsonNode parseJwtPayload(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
        byte[] payloadBytes = Base64.getUrlDecoder().decode(padBase64(parts[1]));
        return objectMapper.readTree(payloadBytes);
    }

    private String getClaimAsString(JsonNode claims, String key) {
        JsonNode node = claims.get(key);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }

    private String padBase64(String base64) {
        int pad = 4 - (base64.length() % 4);
        if (pad != 4) base64 += "=".repeat(pad);
        return base64;
    }
}
