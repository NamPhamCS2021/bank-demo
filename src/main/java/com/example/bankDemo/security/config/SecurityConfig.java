package com.example.bankDemo.security.config;


import com.example.bankDemo.security.filter.JwtFilter;
import com.example.bankDemo.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;

    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors ->cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exceptionHandler ->exceptionHandler.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("swagger-config/**").permitAll()
                    .requestMatchers("swagger-ui/**").permitAll()
                    .requestMatchers("v3/api-docs/**").permitAll()
                    .requestMatchers("swagger-ui.html").permitAll()
                    .requestMatchers("swagger-resources/**").permitAll()
                    .requestMatchers("webjars/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/api/data/**").permitAll()
                    .requestMatchers("/api/auth/aLogin").permitAll()
                    .requestMatchers("/api/auth/aSignup").permitAll()
                    .requestMatchers(
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/webjars/**",
                            "/favicon.ico",
                            "/manifest.json",
                            "/react/**",        // if you serve react.development.js under /react/
                            "/babel/**"         // if you serve babel.min.js under /babel/
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/customers").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/customers/{id}").hasRole("ADMIN")
                    .requestMatchers("/api/customers/search").hasRole("ADMIN")
                    .requestMatchers("/api/customers/**").authenticated()
                    .requestMatchers("/api/accounts/search").hasRole("ADMIN")
                    .requestMatchers("/api/accounts/status/{accountNumber}").hasRole("ADMIN")
                    .requestMatchers("/api/accounts/accountLimit/{accountNumber}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/accounts").hasRole("ADMIN")
                    .requestMatchers("/api/transactions/search").hasRole("ADMIN")
                    .requestMatchers("/api/accountStatusHistory/search").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/alerts").hasRole("ADMIN")
                    .requestMatchers("/api/alerts/search").hasRole("ADMIN")
                    .requestMatchers("/api/payments/search").hasRole("ADMIN")
                    .requestMatchers("/api/report/**").hasRole("ADMIN")
                    .anyRequest().authenticated());
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
}
