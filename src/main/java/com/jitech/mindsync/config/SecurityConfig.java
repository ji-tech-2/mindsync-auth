package com.jitech.mindsync.config;

import com.jitech.mindsync.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;

        @Value("${app.cors.allowed-origins}")
        private String allowedOriginsString;

        public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable()) // Disabled for stateless JWT + SameSite cookies
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.deny())
                                                .xssProtection(xss -> xss.headerValue(
                                                                org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                                                .contentTypeOptions(contentType -> contentType.disable())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)) // 1 year
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'")))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .logout(logout -> logout.disable()) // Disable default logout to use custom endpoint
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(request -> "OPTIONS".equals(request.getMethod()))
                                                .permitAll() // Allow CORS preflight
                                                .requestMatchers("/register", "/login", "/logout").permitAll() // Allow
                                                                                                               // auth
                                                                                                               // endpoints
                                                .requestMatchers("/profile/request-otp", "/profile/request-signup-otp",
                                                                "/profile/verify-otp", "/profile/reset-password")
                                                .permitAll() // Public OTP endpoints
                                                .requestMatchers("/test-email").permitAll() // test email endpoint
                                                .requestMatchers("/test-otp").permitAll() // test otp endpoint
                                                .requestMatchers("/test-verify-otp").permitAll() // test verify otp
                                                                                                 // endpoint
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/actuator/**").permitAll() // Izinkan akses ke
                                                                                             // actuator untuk
                                                                                             // monitoring
                                                .anyRequest().authenticated());

                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Parse comma-separated origins from properties
                String[] origins = allowedOriginsString.split(",");
                configuration.setAllowedOrigins(Arrays.asList(
                                Arrays.stream(origins)
                                                .map(String::trim)
                                                .toArray(String[]::new)));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList(
                                "Content-Type",
                                "Authorization",
                                "Accept",
                                "Origin",
                                "X-Requested-With",
                                "Cache-Control"));
                configuration.setExposedHeaders(Arrays.asList(
                                "Content-Type",
                                "Authorization"));
                configuration.setMaxAge(3600L); // Cache preflight for 1 hour
                configuration.setAllowCredentials(true); // Enable credentials for cookies
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}