package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.JoinDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.WithdrawalDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import com.jeju.nanaland.domain.member.entity.WithdrawalType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.member.repository.MemberWithdrawalRepository;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.UnauthorizedException;
import com.jeju.nanaland.global.util.JwtUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberLoginService {

  private final MemberRepository memberRepository;
  private final MemberWithdrawalRepository memberWithdrawalRepository;
  private final LanguageRepository languageRepository;
  private final JwtUtil jwtUtil;
  private final MemberConsentService memberConsentService;
  private final ImageFileService imageFileService;
  private final MemberTravelTypeRepository memberTravelTypeRepository;

  @Transactional
  public JwtDto join(JoinDto joinDto, MultipartFile multipartFile) {
    Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(
        Provider.valueOf(joinDto.getProvider()),
        joinDto.getProviderId());

    if (memberOptional.isPresent()) {
      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    String nickname = validateNickname(joinDto);
    ImageFile profileImageFile = getProfileImageFile(multipartFile);
    Member member = createMember(joinDto, profileImageFile, nickname);
    memberConsentService.createMemberConsents(member, joinDto.getConsentItems());
    return getJwtDto(member);
  }

  private String validateNickname(JoinDto joinDto) {
    /**
     * TODO : 닉네임 글자 제한 확인
     */
    String nickname = joinDto.getNickname();
    if (Provider.valueOf(joinDto.getProvider()) == Provider.GUEST) {
      nickname = UUID.randomUUID().toString().substring(0, 16);
    }

    Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
    if (memberOptional.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
    return nickname;
  }

  private ImageFile getProfileImageFile(MultipartFile multipartFile) {
    if (multipartFile == null) {
      return imageFileService.getRandomProfileImageFile();
    }
    return imageFileService.uploadAndSaveImageFile(multipartFile, true);
  }

  private Member createMember(JoinDto joinDto, ImageFile imageFile, String nickname) {

    Language language = languageRepository.findByLocale(Locale.valueOf(joinDto.getLocale()));
    MemberTravelType memberTravelType = memberTravelTypeRepository.findByTravelType(
        TravelType.NONE);

    // Enum에는 있지만 DB에는 없는 경우
    if (language == null) {
      String errorMessage = joinDto.getLocale() + "에 해당하는 언어 정보가 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }
    if (memberTravelType == null) {
      String errorMessage = TravelType.NONE + "에 해당하는 타입 정보가 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    Member member = Member.builder()
        .language(language)
        .email(joinDto.getEmail())
        .profileImageFile(imageFile)
        .nickname(nickname)
        .gender(joinDto.getGender())
        .birthDate(joinDto.getBirthDate())
        .provider(Provider.valueOf(joinDto.getProvider()))
        .providerId(joinDto.getProviderId())
        .memberTravelType(memberTravelType)
        .build();
    return memberRepository.save(member);
  }

  @Transactional
  public JwtDto login(LoginDto loginDto) {

    Member member = memberRepository.findByProviderAndProviderId(
            Provider.valueOf(loginDto.getProvider()), loginDto.getProviderId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    updateMemberActive(member);
    updateLanguageDifferent(loginDto, member);
    return getJwtDto(member);
  }

  private JwtDto getJwtDto(Member member) {
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
  public void updateMemberActive(Member member) {
    if (member.getStatus().equals(Status.INACTIVE)) {
      member.updateStatus(Status.ACTIVE);
    }
  }

  @Transactional
  public void updateLanguageDifferent(LoginDto loginDto, Member member) {
    Locale locale = Locale.valueOf(loginDto.getLocale());
    if (!member.getLanguage().getLocale().equals(locale)) {
      Language language = languageRepository.findByLocale(locale);

      member.updateLanguage(language);
    }
  }

  public JwtDto reissue(String bearerRefreshToken) {
    String refreshToken = jwtUtil.resolveToken(bearerRefreshToken);

    if (!jwtUtil.verifyRefreshToken(refreshToken)) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtUtil.findRefreshTokenById(memberId);

    if (!refreshToken.equals(savedRefreshToken)) {
      jwtUtil.deleteRefreshToken(memberId);
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    return getJwtDto(member);
  }

  public void logout(MemberInfoDto memberInfoDto, String bearerAccessToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    jwtUtil.setBlackList(accessToken);
    String memberId = String.valueOf(memberInfoDto.getMember().getId());
    if (jwtUtil.findRefreshTokenById(memberId) != null) {
      jwtUtil.deleteRefreshToken(memberId);
    }
  }

  @Transactional
  public void withdrawal(MemberInfoDto memberInfoDto, WithdrawalDto withdrawalType) {

    memberInfoDto.getMember().updateStatus(Status.INACTIVE);

    MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
        .member(memberInfoDto.getMember())
        .withdrawalType(WithdrawalType.valueOf(withdrawalType.getWithdrawalType()))
        .build();
    memberWithdrawalRepository.save(memberWithdrawal);
  }

  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void deleteWithdrawalMemberInfo() {
    List<Member> members = memberRepository.findInactiveMembersForWithdrawalDate();

    if (!members.isEmpty()) {
      members.forEach(Member::updatePersonalInfo);
    }
  }
}
