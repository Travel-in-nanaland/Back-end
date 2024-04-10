package com.jeju.nanaland.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;


@Slf4j
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {

  private final JwtUtil jwtUtil;

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager,
      JwtUtil jwtUtil) {

    super(authenticationManager);
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = jwtUtil.resolveToken(bearerToken);

    if (StringUtils.hasLength(token) && jwtUtil.verifyAccessToken(token)) {
      Authentication authentication = jwtUtil.getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    chain.doFilter(request, response);
  }
}
