package com.LeaveManagement.EmployeeLeaveManagement.config;

import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import com.LeaveManagement.EmployeeLeaveManagement.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository userRepository;

    // 1. UserDetailService
    // Tell Spring how to load a user given a username (email in our case)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username
                ));
    }

    // 2. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Authentication Provider
    // Wired both UserDetailService + PasswordEncoder
    // Used by AuthenticationManager to verify credentials on login

    // 4. AuthenticationManager
    // Used in AuthService to trigger authentication (validate email+password)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5. SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // Disable CSRF — not needed for stateless REST APIs using JWT
                .csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("Invalid email or password");
                        })
                )

                // Define which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no token required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/notifications/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()                                    // ← add this block

                        // Role-based access
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                        // Every other request must be authenticated
                        .anyRequest().authenticated()
                )

                // Stateless session — no HttpSession, no cookies
                // Each request MUST carry its own JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                // Add JwtAuthFilter BEFORE Spring's UsernamePasswordAuthenticationFilter
                // so our filter runs first and populates the SecurityContext from JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
