package com.jeju.nanaland.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

  private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
  private final SecretKey secretKey;
  private final SecretKey secretKey2;
  private final MemberDetailsService memberDetailsService;
  @Value("${jwt.access.expiration}")
  private Long accessExpirationPeriod;
  @Value("${jwt.refresh.expiration}")
  private Long refreshExpirationPeriod;

  private SecretKey getSecretKey() {
    return secretKey;
  }

  public String getAccessToken(Long memberId) {
    Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));

    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessExpirationPeriod);

    return Jwts.builder()
        .setSubject(ACCESS_TOKEN_SUBJECT)
        .setClaims(claims)
        .setExpiration(expiration)
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public String getRefreshToken(Long memberId) {
    Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));

    Date now = new Date();
    Date expiration = new Date(now.getTime() + refreshExpirationPeriod);

    return Jwts.builder()
        .setSubject(REFRESH_TOKEN_SUBJECT)
        .setClaims(claims)
        .setExpiration(expiration)
        .signWith(secretKey2, SignatureAlgorithm.HS512)
        .compact();
  }

  public boolean verifyAccessToken(String accessToken) {
    try {
      Jws<Claims> claimsJws = Jwts.parserBuilder()
          .setSigningKey(getSecretKey())
          .build()
          .parseClaimsJws(accessToken);

      return !claimsJws.getBody().getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public String getMemberId(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = memberDetailsService.loadUserByUsername(getMemberId(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }
}
