package com.jeju.nanaland.global.util;

import com.jeju.nanaland.domain.member.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
  private static final String REFRESH_KEY = "REFRESH:";
  private static final String AUTHORITIES_KEY = "auth";
  private static final String BLACK_LIST_VAL = "BLACKLIST";
  private static final long BLACK_LIST_EXPIRE = 10 * 60L;
  private static final long MAX_REFRESH_TOKENS = 10;
  private final SecretKey secretKey;
  private final SecretKey secretKey2;
  private final RedisUtil redisUtil;
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

  // AccessToken 생성
  public String createAccessToken(String memberId, Set<Role> roleSet) {
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

  // RefreshToken 생성
  public String createRefreshToken(String memberId, Set<Role> roleSet) {
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

    redisUtil.trimAndPushLeft(REFRESH_KEY + memberId, MAX_REFRESH_TOKENS, refreshToken);
    return refreshToken;
  }

  // AccessToken 유효성 확인
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

  // AccessToken에서 memberId 찾기
  public String getMemberIdFromAccess(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // JWT를 파싱하여, 사용자 인증 정보를 생성
  public Authentication getAuthentication(String token) {
    // JWT 토큰을 파싱하여 클레임(claims)을 추출
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

    // 클레임에서 권한(authorities) 정보를 가져와 GrantedAuthority 리스트로 변환
    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    // 토큰에서 사용자 ID를 추출하고, 권한과 함께 UsernamePasswordAuthenticationToken 객체로 반환
    return new UsernamePasswordAuthenticationToken(getMemberIdFromAccess(token), "",
        authorities);
  }

  // bearerToken에서 실제 JWT 추출
  public String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring("Bearer ".length());
    }
    return null;
  }

  // RefreshToken 유효성 확인
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

  // Redis에서 RefreshToken 찾기
  public String findRefreshToken(String memberId) {
    return redisUtil.getRecentDataFromList(REFRESH_KEY + memberId);
  }

  // RefreshToken에서 memberId 찾기
  public String getMemberIdFromRefresh(String refreshToken) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey2())
        .build()
        .parseClaimsJws(refreshToken)
        .getBody()
        .getSubject();
  }

  // Redis에서 RefreshToken 삭제
  public void deleteRefreshToken(String memberId) {
    redisUtil.deleteData(REFRESH_KEY + memberId);
  }

  // AccessToken을 BlackList로 등록
  public void setBlackList(String accessToken) {
    redisUtil.setExpiringValue(accessToken, BLACK_LIST_VAL, BLACK_LIST_EXPIRE);
  }
}
