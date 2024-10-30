package com.jeju.nanaland.global.auth;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.UnauthorizedException;
import com.jeju.nanaland.global.util.JwtUtil;
import com.jeju.nanaland.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthMemberArgumentResolver implements HandlerMethodArgumentResolver {

  private final RedisUtil redisUtil;
  private final JwtUtil jwtUtil;
  private final MemberRepository memberRepository;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    boolean hasAnnotation = parameter.getParameterAnnotation(AuthMember.class) != null;
    boolean hasMemberType = MemberInfoDto.class.isAssignableFrom(parameter.getParameterType());
    return hasAnnotation && hasMemberType;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    String bearerAccessToken = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);

    // null이 아닌 경우, 로그아웃을 통해 이미 블랙리스트에 담긴 것을 의미
    if (redisUtil.getValue(accessToken) != null) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromAccess(accessToken);

    MemberInfoDto memberInfoDto = memberRepository.findMemberInfoDto(
        Long.valueOf(memberId));

    if (memberInfoDto == null) {
      throw new NotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
    return memberInfoDto;
  }
}
