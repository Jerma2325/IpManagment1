package com.example.backend.filter;

import com.example.backend.service.JwtService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String authorizationHeader = request.getHeader("Authorization");

        System.out.println("JWT Filter - Processing request: " + requestURI);
        System.out.println("JWT Filter - Authorization header: " + (authorizationHeader != null ? "present" : "missing"));

        // skip authentication for login and register endpoints
        if (requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/register")) {
            System.out.println("JWT Filter - Skipping authentication for " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        String username = null;
        String jwt = null;
        boolean hasExistingAuth = SecurityContextHolder.getContext().getAuthentication() != null;
        String currentAuth = hasExistingAuth ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "none";
        System.out.println("JWT Filter - Existing authentication: " + currentAuth);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT Filter - Processing JWT token");

            try {
                username = jwtService.extractUsername(jwt);
                System.out.println("JWT Filter - Extracted username: " + username);
            } catch (Exception e) {
                System.err.println("JWT Filter - Invalid JWT token: " + e.getMessage());
            }
        } else {
            System.out.println("JWT Filter - No JWT token found in request");
        }

        if (username != null && (!hasExistingAuth || "anonymousUser".equals(currentAuth))) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter - Loaded user details for: " + userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter - Authentication successful for user: " + username);
                } else {
                    System.err.println("JWT Filter - Token validation failed for user: " + username);
                }
            } catch (Exception e) {
                System.err.println("JWT Filter - Error authenticating user: " + e.getMessage());
            }
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String authName = SecurityContextHolder.getContext().getAuthentication().getName();
            response.setHeader("X-Debug-Auth", authName);
            System.out.println("JWT Filter - Current authentication after processing: " + authName);
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        boolean shouldSkip = path.startsWith("/h2-console")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register");

        if (shouldSkip) {
            System.out.println("JWT Filter - Skipping filter for path: " + path);
        }

        return shouldSkip;
    }
}