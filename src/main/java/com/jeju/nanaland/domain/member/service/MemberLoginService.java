package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_WTIHDRAWAL_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.JoinDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.WithdrawalDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import com.jeju.nanaland.domain.member.entity.WithdrawalType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberWithdrawalRepository;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.service.FcmTokenService;
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
  private final JwtUtil jwtUtil;
  private final MemberConsentService memberConsentService;
  private final ImageFileService imageFileService;
  private final FcmTokenService fcmTokenService;

  @Transactional
  public JwtDto join(JoinDto joinDto, MultipartFile multipartFile) {
    Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(
        Provider.valueOf(joinDto.getProvider()),
        joinDto.getProviderId());

    if (memberOptional.isPresent()) {
      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    String nickname = determineNickname(joinDto);
    validateNickname(nickname);
    ImageFile profileImageFile = getProfileImageFile(multipartFile);
    Member member = createMember(joinDto, profileImageFile, nickname);
    if (!member.getProvider().equals(Provider.GUEST)) {
      memberConsentService.createMemberConsents(member, joinDto.getConsentItems());
    }

    // fcm 토큰 저장
    fcmTokenService.saveFcmToken(member, joinDto.getFcmToken());

    return getJwtDto(member);
  }

  private String determineNickname(JoinDto joinDto) {
    if (Provider.valueOf(joinDto.getProvider()) == Provider.GUEST) {
      return UUID.randomUUID().toString().substring(0, 12);
    }
    return joinDto.getNickname();
  }

  public void validateNickname(String nickname) {
    Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
    if (memberOptional.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  private ImageFile getProfileImageFile(MultipartFile multipartFile) {
    if (multipartFile == null) {
      return imageFileService.getRandomProfileImageFile();
    }
    return imageFileService.uploadAndSaveImageFile(multipartFile, true);
  }

  private Member createMember(JoinDto joinDto, ImageFile imageFile, String nickname) {

    Language language = Language.valueOf(joinDto.getLocale());
    TravelType noneTravelType = TravelType.NONE;

    Member member = Member.builder()
        .language(language)
        .email(joinDto.getEmail() != null ? joinDto.getEmail() : "ACTIVE@nanaland.com")
        .profileImageFile(imageFile)
        .nickname(nickname)
        .gender(joinDto.getGender())
        .birthDate(joinDto.getBirthDate())
        .provider(Provider.valueOf(joinDto.getProvider()))
        .providerId(joinDto.getProviderId())
        .travelType(noneTravelType)
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

    // fcm 토큰이 없다면 생성, timestamp 갱신
    FcmToken fcmToken = fcmTokenService.getFcmToken(member, loginDto.getFcmToken());
    if (fcmToken == null) {
      fcmToken = fcmTokenService.saveFcmToken(member, loginDto.getFcmToken());
    }
    fcmToken.updateTimestampToNow();

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

      MemberWithdrawal memberWithdrawal = memberWithdrawalRepository.findByMember(member)
          .orElseThrow(() -> new NotFoundException(MEMBER_WTIHDRAWAL_NOT_FOUND.getMessage()));
      memberWithdrawal.updateStatus(Status.INACTIVE);
    }
  }

  @Transactional
  public void updateLanguageDifferent(LoginDto loginDto, Member member) {
    Language language = Language.valueOf(loginDto.getLocale());
    if (!member.getLanguage().equals(language)) {
      member.updateLanguage(language);
    }
  }

  public JwtDto reissue(String bearerRefreshToken, String fcmToken) {
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

    // fcm 토큰이 없다면 생성, timestamp 갱신
    FcmToken fcmTokenInstance = fcmTokenService.getFcmToken(member, fcmToken);
    if (fcmTokenInstance == null) {
      fcmTokenInstance = fcmTokenService.saveFcmToken(member, fcmToken);
    }
    fcmTokenInstance.updateTimestampToNow();

    return getJwtDto(member);
  }

  @Transactional
  public void logout(MemberInfoDto memberInfoDto, String bearerAccessToken, String fcmToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    jwtUtil.setBlackList(accessToken);
    String memberId = String.valueOf(memberInfoDto.getMember().getId());
    if (jwtUtil.findRefreshTokenById(memberId) != null) {
      jwtUtil.deleteRefreshToken(memberId);
    }

    // fcm 토큰 삭제
    FcmToken fcmTokenInstance = fcmTokenService.getFcmToken(memberInfoDto.getMember(), fcmToken);
    fcmTokenService.deleteFcmToken(fcmTokenInstance);
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

  @Transactional
  public void forceWithdrawal(String bearerAccessToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);

    if (!jwtUtil.verifyAccessToken(accessToken)) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromAccess(accessToken);

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    MemberWithdrawal memberWithdrawal = memberWithdrawalRepository.findByMember(member)
        .orElseThrow(() -> new NotFoundException(MEMBER_WTIHDRAWAL_NOT_FOUND.getMessage()));
    memberWithdrawal.updateWithdrawalDate(); // 탈퇴일을 4개월 전으로 변경
    deleteWithdrawalMemberInfo();
  }
}
