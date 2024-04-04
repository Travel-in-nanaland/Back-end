package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.dto.response.ImageFileResponse;
import com.jeju.nanaland.domain.common.dto.response.LanguageResponse;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.request.LoginRequest;
import com.jeju.nanaland.domain.member.dto.response.LoginResponse;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.jwt.JwtProvider;
import com.jeju.nanaland.global.jwt.dto.JwtResponse;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final LanguageRepository languageRepository;
  private final ImageFileRepository imageFileRepository;

  public LoginResponse login(LoginRequest loginRequest) {

    Member member = getOrCreateMember(loginRequest);

    String accessToken = jwtProvider.getAccessToken(member.getEmail());
    String refreshToken = jwtProvider.getRefreshToken(member.getEmail());

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
        .email(member.getEmail())
        .nickname(member.getNickname())
        .description(member.getDescription())
        .provider(member.getProvider())
        .providerId(member.getProviderId())
        .build();
  }

  private Member getOrCreateMember(LoginRequest loginRequest) {
    Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(
        loginRequest.getProvider(),
        loginRequest.getProviderId());

    if (memberOptional.isEmpty()) {
      return createMember(loginRequest);
    }
    return memberOptional.get();
  }

  private Member createMember(LoginRequest loginRequest) {
    Language language = languageRepository.findByLocale(loginRequest.getLocale());

    ImageFile profileImageFile = getRandomProfileImageFile();

    String nickname = loginRequest.getProvider() + "_" + loginRequest.getProviderId();
    String password = loginRequest.getEmail() + "_" + loginRequest.getProviderId();

    Member member = Member.builder()
        .language(language)
        .email(loginRequest.getEmail())
        .password(passwordEncoder.encode(password))
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
}
