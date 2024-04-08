package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequestDto.LoginRequest;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.jwt.JwtProvider;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberLoginService {

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;
  private final LanguageRepository languageRepository;
  private final ImageFileRepository imageFileRepository;

  @Transactional
  public JwtDto login(LoginRequest loginRequest) {

    Member member = getOrCreateMember(loginRequest);

    String accessToken = jwtProvider.getAccessToken(String.valueOf(member.getId()));
    String refreshToken = jwtProvider.getRefreshToken(String.valueOf(member.getId()));

    return JwtDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional
  public Member getOrCreateMember(LoginRequest loginRequest) {
    Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(
        loginRequest.getProvider(),
        loginRequest.getProviderId());

    if (memberOptional.isEmpty()) {
      return createMember(loginRequest);
    }
    Member member = memberOptional.get();
    updateEmailDifferent(loginRequest, member);
    return member;
  }

  private Member createMember(LoginRequest loginRequest) {
    Language language = languageRepository.findByLocale(loginRequest.getLocale());

    ImageFile profileImageFile = getRandomProfileImageFile();

    String nickname = loginRequest.getProvider() + "_" + loginRequest.getProviderId();

    Member member = Member.builder()
        .language(language)
        .email(loginRequest.getEmail())
        .profileImageFile(profileImageFile)
        .nickname(nickname)
        .gender(loginRequest.getGender())
        .birthDate(loginRequest.getBirthDate())
        .provider(loginRequest.getProvider())
        .providerId(loginRequest.getProviderId())
        .build();
    return memberRepository.save(member);
  }

  // 임시로 만든 랜덤 프로필 사진
  private ImageFile getRandomProfileImageFile() {
    Random random = new Random();
    long randomId = random.nextInt(3) + 1;
    return imageFileRepository.findById(randomId).get();
  }

  @Transactional
  public void updateEmailDifferent(LoginRequest loginRequest, Member member) {
    if (!member.getEmail().equals(loginRequest.getEmail())) {
      member.updateEmail(loginRequest.getEmail());
    }
  }

  public String reissue(String bearerRefreshToken) {
    String refreshToken = jwtProvider.resolveToken(bearerRefreshToken);

    if (!jwtProvider.verifyRefreshToken(refreshToken)) {
      throw new BadRequestException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtProvider.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtProvider.findRefreshTokenById(memberId);

    if (!refreshToken.equals(savedRefreshToken)) {
      throw new BadRequestException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    return jwtProvider.getAccessToken(memberId);
  }
}
