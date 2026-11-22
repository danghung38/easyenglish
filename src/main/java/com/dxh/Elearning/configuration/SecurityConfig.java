package com.dxh.Elearning.configuration;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
//phân quyền trên method
@EnableMethodSecurity
public class SecurityConfig {


    private final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth/login", "/auth/introspect", "/auth/logout",
            "/auth/login/google", "/auth/login-2fa", "/auth/login/facebook", "/auth/refresh", "/users"
            ,"/users/reset-password","/users/forgot-password",
            "/users/verify-register", "/users/resend-verification",
            "/ielts/speaking/evaluate-by-question",  // IELTS Speaking evaluation
            "/schedule"  // Schedule event creation (temporarily public for testing)
    };

    private final String[] PUBLIC_GET_ENDPOINTS = {
            "/stories", "/stories/*",
            "/users/list",
            "/exams/list",
            "/examparts/list",
            "/questions/list",
            // ExoPlayer/Glide load static IELTS media directly and cannot attach
            // the Retrofit bearer token. Only generated read-only media is public.
            "/ielts/audio/**",
            "/ielts/images/**",
            "/users/list-with-sort-by-multiple-columns",
            "/users/advance-search-with-specification"
    };

    private final String[] API_TEST = {
            "/**"
    };

    private final String[] API_DOC_ENDPOINTS = {
            "/api/swagger-ui.html",
            "/api/swagger-ui/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v3/api-docs/**",
            "/api/swagger-resources/**",
            "/api/webjars/**",
            "/users/confirm-email"

    };



    @Autowired
    private CustomJwtDecoder customJwtDecoder;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> {})  // Enable CORS with default configuration
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow preflight
                                .requestMatchers(HttpMethod.POST, "/stories").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/stories/*").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/stories/*").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                                .requestMatchers(API_DOC_ENDPOINTS).permitAll()
//                                websocket
                                .requestMatchers("/ws/**", "/topic/**", "/queue/**", "/user/**").permitAll()
                                .requestMatchers("/admin-notify.html").permitAll()
//                                .requestMatchers(API_TEST).permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                        jwtConfigurer.decoder(customJwtDecoder)
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                );

        return httpSecurity.build();
    }



    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

}
