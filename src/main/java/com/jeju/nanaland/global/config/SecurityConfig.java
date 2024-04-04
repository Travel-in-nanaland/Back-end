package com.jeju.nanaland.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju.nanaland.global.jwt.JWTAuthenticationFilter;
import com.jeju.nanaland.global.jwt.JwtProvider;
import com.jeju.nanaland.global.jwt.handler.CustomAccessDeniedHandler;
import com.jeju.nanaland.global.jwt.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      AuthenticationConfiguration authenticationConfiguration) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .headers(c -> c.frameOptions(
            FrameOptionsConfig::disable).disable())
        .sessionManagement(c ->
            c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http
        .authorizeHttpRequests(authHttpRequests -> authHttpRequests
            .requestMatchers("/login")
            .permitAll()
            .anyRequest().authenticated());

    http
        .addFilterBefore(
            new JWTAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(),
                jwtProvider),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper))
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper)));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
