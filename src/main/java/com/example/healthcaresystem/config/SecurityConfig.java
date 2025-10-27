package com.example.healthcaresystem.config;

import com.example.healthcaresystem.filter.JwtAuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection for required dependencies
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, 
                     UserDetailsService userDetailsService,
                     PasswordEncoder passwordEncoder) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
}
    /* 
     * Main security configuration
     * Defines endpoint access rules and JWT filter setup
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT)
            .csrf(csrf -> csrf.disable())

            // Configure endpoint authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/welcome", "/api/auth/register/**", "/api/auth/login").permitAll()
                .requestMatchers("/api/doctors", "/api/doctors/*").permitAll() // Allow public access to doctor search
                
                // Doctor-specific endpoints
                .requestMatchers("/api/doctors/*/slots").hasAuthority("ROLE_DOCTOR")
                .requestMatchers("/api/appointments/doctor/**").hasAuthority("ROLE_DOCTOR")
                .requestMatchers("/api/appointments/*/prescription", "/api/appointments/*/complete").hasAuthority("ROLE_DOCTOR")
                
                // Patient-specific endpoints
                .requestMatchers("/api/appointments/patient/**").hasAuthority("ROLE_PATIENT")
                .requestMatchers("/api/medical-records/patient/**").hasAuthority("ROLE_PATIENT")
                
                // Shared endpoints (both doctors and patients can access)
                .requestMatchers("/api/appointments/book", "/api/appointments/cancel").hasAnyAuthority("ROLE_DOCTOR", "ROLE_PATIENT")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Stateless session (required for JWT)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Set custom authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add JWT filter before Spring Security's default filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /* 
     * Authentication provider configuration
     * Links UserDetailsService and PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /* 
     * Authentication manager bean
     * Required for programmatic authentication (e.g., in /generateToken)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
