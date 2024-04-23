package com.jeju.nanaland.global.jwt;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
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
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    String bearerAccessToken = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    String memberId = jwtUtil.getMemberIdFromAccess(accessToken);
    return memberRepository.findMemberWithLanguage(Long.valueOf(memberId));
  }
}
