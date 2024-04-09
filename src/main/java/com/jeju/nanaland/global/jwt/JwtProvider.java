package com.jeju.nanaland.global.jwt;

import com.jeju.nanaland.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

  private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
  private static final String REDIS_KEY = "REFRESH:";
  private static final String AUTHORITIES_KEY = "auth";
  private final SecretKey secretKey;
  private final SecretKey secretKey2;
  private final RedisTemplate<String, String> redisTemplate;
  @Value("${jwt.access.expiration}")
  private Long accessExpirationPeriod;
  @Value("${jwt.refresh.expiration}")
  private Long refreshExpirationPeriod;

  private SecretKey getSecretKey() {
    return secretKey;
  }

  private SecretKey getSecretKey2() {
    return secretKey2;
  }

  public String getAccessToken(String memberId, Set<Role> roleSet) {
    Claims claims = Jwts.claims().setSubject(memberId);
    String authorities = roleSet.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessExpirationPeriod);

    return Jwts.builder()
        .setSubject(ACCESS_TOKEN_SUBJECT)
        .setClaims(claims)
        .claim(AUTHORITIES_KEY, authorities)
        .setExpiration(expiration)
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public String getRefreshToken(String memberId, Set<Role> roleSet) {
    Claims claims = Jwts.claims().setSubject(memberId);
    String authorities = roleSet.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
    Date now = new Date();
    Date expiration = new Date(now.getTime() + refreshExpirationPeriod);

    String refreshToken = Jwts.builder()
        .setSubject(REFRESH_TOKEN_SUBJECT)
        .setClaims(claims)
        .claim(AUTHORITIES_KEY, authorities)
        .setExpiration(expiration)
        .signWith(secretKey2, SignatureAlgorithm.HS512)
        .compact();

    redisTemplate.opsForValue().set(
        REDIS_KEY + memberId,
        refreshToken,
        refreshExpirationPeriod,
        TimeUnit.MILLISECONDS
    );
    return refreshToken;
  }

  public boolean verifyAccessToken(String accessToken) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSecretKey())
          .build()
          .parseClaimsJws(accessToken);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getMemberIdFromAccess(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    return new UsernamePasswordAuthenticationToken(getMemberIdFromAccess(token), "",
        authorities);
  }

  public String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring("Bearer ".length());
    }
    return null;
  }

  public boolean verifyRefreshToken(String refreshToken) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSecretKey2())
          .build()
          .parseClaimsJws(refreshToken);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String findRefreshTokenById(String memberId) {
    return redisTemplate.opsForValue().get(REDIS_KEY + memberId);
  }

  public String getMemberIdFromRefresh(String refreshToken) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey2())
        .build()
        .parseClaimsJws(refreshToken)
        .getBody()
        .getSubject();
  }
}
