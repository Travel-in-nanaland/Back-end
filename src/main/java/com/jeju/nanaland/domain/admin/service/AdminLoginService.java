package com.jeju.nanaland.domain.admin.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.Role;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.exception.ForbiddenException;
import com.jeju.nanaland.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminLoginService {

  private final MemberLoginService memberLoginService;
  private final MemberRepository memberRepository;

  public JwtDto adminLogin(LoginDto loginDto) {

    Member member = memberRepository.findByProviderAndProviderId(
            Provider.valueOf(loginDto.getProvider()), loginDto.getProviderId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    // ADMIN 권한이 없다면 error
    if (!member.getRoleSet().contains(Role.ROLE_ADMIN)) {
      throw new ForbiddenException("관리자 권한이 없습니다.");
    }

    return memberLoginService.login(loginDto);
  }
}
