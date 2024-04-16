package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepositoryCustom;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.jwt.JwtUtil;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberLoginService {

  private final MemberRepository memberRepository;
  private final MemberRepositoryCustom memberRepositoryCustom;
  private final LanguageRepository languageRepository;
  private final ImageFileRepository imageFileRepository;
  private final JwtUtil jwtUtil;

  @Transactional
  public JwtDto login(LoginDto loginDto) {

    Member member = getOrCreateMember(loginDto);

    String accessToken = jwtUtil.getAccessToken(String.valueOf(member.getId()),
        member.getRoleSet());
    String refreshToken = jwtUtil.getRefreshToken(String.valueOf(member.getId()),
        member.getRoleSet());

    return JwtDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional
  public Member getOrCreateMember(LoginDto loginDto) {
    Optional<Member> memberOptional = memberRepository.findByEmailAndProviderAndProviderId(
        loginDto.getEmail(),
        Provider.valueOf(loginDto.getProvider()),
        loginDto.getProviderId());

    if (memberOptional.isEmpty()) {
      return createMember(loginDto);
    }
    Member member = memberOptional.get();
    updateEmailDifferent(loginDto, member);
    return member;
  }

  private Member createMember(LoginDto loginDto) {

    Optional<Member> memberOptional = memberRepositoryCustom.findDuplicateMember(
        loginDto.getEmail(),
        Provider.valueOf(loginDto.getProvider()),
        loginDto.getProviderId());

    if (memberOptional.isPresent()) {
      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    Language language = languageRepository.findByLocale(Locale.valueOf(loginDto.getLocale()));

    ImageFile profileImageFile = getRandomProfileImageFile();

    String nickname = loginDto.getProvider() + "_" + loginDto.getProviderId();

    Member member = Member.builder()
        .language(language)
        .email(loginDto.getEmail())
        .profileImageFile(profileImageFile)
        .nickname(nickname)
        .gender(loginDto.getGender())
        .birthDate(loginDto.getBirthDate())
        .provider(Provider.valueOf(loginDto.getProvider()))
        .providerId(loginDto.getProviderId())
        .build();
    return memberRepository.save(member);
  }

  // 임시로 만든 랜덤 프로필 사진
  private ImageFile getRandomProfileImageFile() {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("originUrl")
        .thumbnailUrl("thumbnailUrl")
        .build();
    return imageFileRepository.save(imageFile);
  }

  @Transactional
  public void updateEmailDifferent(LoginDto loginDto, Member member) {
    if (!member.getEmail().equals(loginDto.getEmail())) {
      member.updateEmail(loginDto.getEmail());
    }
  }

  public String reissue(String bearerRefreshToken) {
    String refreshToken = jwtUtil.resolveToken(bearerRefreshToken);

    if (!jwtUtil.verifyRefreshToken(refreshToken)) {
      throw new BadRequestException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtUtil.findRefreshTokenById(memberId);

    if (!refreshToken.equals(savedRefreshToken)) {
      throw new BadRequestException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    return jwtUtil.getAccessToken(memberId, member.getRoleSet());
  }
}
