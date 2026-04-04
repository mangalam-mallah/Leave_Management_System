package com.LeaveManagement.EmployeeLeaveManagement.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getServletPath().startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Read the authentication header
        final String authHeader = request.getHeader("Authorization");
        // 2. If no bearer token present, skip the filter entirely
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT (strip "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        try {
            // 4. Extract email(subject) from token
            final String userEmail = jwtUtil.extractUsername(jwt);

            // 5. Only authenticate if not already authenticated in this request
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 7. Validate token (signature + expiry + username)
                if(jwtUtil.isTokenValid(jwt, userDetails)) {
                    // 8.  Create authentication token and set it in SecurityContext
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Store it SecurityContext - marks this request as authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }


        } catch (JwtException e) {
            // Invalid/expired/malformed token — let the request continue unauthenticated
            // Spring Security will then return 401 for protected routes
            SecurityContextHolder.clearContext();
        }

        // 10. Pass to next filter in chain
        filterChain.doFilter(request, response);
    }
}
