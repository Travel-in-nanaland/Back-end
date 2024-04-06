package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.dto.response.ImageFileResponse;
import com.jeju.nanaland.domain.common.dto.response.LanguageResponse;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequestDto.LoginRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponseDto.LoginResponse;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.jwt.JwtProvider;
import com.jeju.nanaland.global.jwt.dto.JwtResponse;
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
  public LoginResponse login(LoginRequest loginRequest) {

    Member member = getOrCreateMember(loginRequest);

    String accessToken = jwtProvider.getAccessToken(member.getId());
    String refreshToken = jwtProvider.getRefreshToken(member.getId());

    // TODO: provider과 provider_id로 이미 가입된 회원이지만 이메일이 변경된 경우, 이메일 update
    // TODO: refreshToken 보관하기

    JwtResponse jwtResponse = JwtResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    LanguageResponse languageResponse = LanguageResponse.builder()
        .locale(member.getLanguage().getLocale())
        .dateFormat(member.getLanguage().getDateFormat())
        .build();

    ImageFileResponse imageFileResponse = ImageFileResponse.builder()
        .id(member.getProfileImageFile().getId())
        .thumbnailUrl(member.getProfileImageFile().getThumbnailUrl())
        .originUrl(member.getProfileImageFile().getOriginUrl())
        .build();

    return LoginResponse.builder()
        .jwtResponse(jwtResponse)
        .languageResponse(languageResponse)
        .imageFileResponse(imageFileResponse)
        .memberId(member.getId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .description(member.getDescription())
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
    updateEmailDiffrent(loginRequest, member);
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
  public void updateEmailDiffrent(LoginRequest loginRequest, Member member) {
    if (!member.getEmail().equals(loginRequest.getEmail())) {
      member.updateEmail(loginRequest.getEmail());
    }
  }
}
