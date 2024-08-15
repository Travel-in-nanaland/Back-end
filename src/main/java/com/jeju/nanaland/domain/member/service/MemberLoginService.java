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

  // 회원 가입
  @Transactional
  public JwtDto join(JoinDto joinDto, MultipartFile multipartFile) {
    Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(
        Provider.valueOf(joinDto.getProvider()),
        joinDto.getProviderId());

    // 이미 가입한 경우
    if (memberOptional.isPresent()) {
      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    String nickname = determineNickname(joinDto);
    validateNickname(nickname);
    ImageFile profileImageFile = getProfileImageFile(multipartFile);
    Member member = createMember(joinDto, profileImageFile, nickname);
    // GUEST가 아닌 경우, 이용약관 저장
    if (!member.getProvider().equals(Provider.GUEST)) {
      memberConsentService.createMemberConsents(member, joinDto.getConsentItems());
    }
    return getJwtDto(member);
  }

  // 랜덤 닉네임 설정
  private String determineNickname(JoinDto joinDto) {
    if (Provider.valueOf(joinDto.getProvider()) == Provider.GUEST) {
      return UUID.randomUUID().toString().substring(0, 12);
    }
    return joinDto.getNickname();
  }

  // 닉네임 중복 확인
  public void validateNickname(String nickname) {
    Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
    if (memberOptional.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  // 프로필 이미지 파일 설정
  private ImageFile getProfileImageFile(MultipartFile multipartFile) {
    if (multipartFile == null) {
      return imageFileService.getRandomProfileImageFile();
    }
    return imageFileService.uploadAndSaveImageFile(multipartFile, true);
  }

  // 회원 생성
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

  // 로그인
  @Transactional
  public JwtDto login(LoginDto loginDto) {

    Member member = memberRepository.findByProviderAndProviderId(
            Provider.valueOf(loginDto.getProvider()), loginDto.getProviderId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    updateMemberActive(member);
    updateLanguageDifferent(loginDto, member);
    return getJwtDto(member);
  }

  // JWT 생성
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

  // 회원 상태 재활성화 및 탈퇴 비활성화
  @Transactional
  public void updateMemberActive(Member member) {
    if (member.getStatus().equals(Status.INACTIVE)) {
      member.updateStatus(Status.ACTIVE);

      MemberWithdrawal memberWithdrawal = memberWithdrawalRepository.findByMember(member)
          .orElseThrow(() -> new NotFoundException(MEMBER_WTIHDRAWAL_NOT_FOUND.getMessage()));
      memberWithdrawal.updateStatus(Status.INACTIVE);
    }
  }

  // 언어 설정 변경
  @Transactional
  public void updateLanguageDifferent(LoginDto loginDto, Member member) {
    Language language = Language.valueOf(loginDto.getLocale());
    if (!member.getLanguage().equals(language)) {
      member.updateLanguage(language);
    }
  }

  // JWT 재발급
  public JwtDto reissue(String bearerRefreshToken) {
    String refreshToken = jwtUtil.resolveToken(bearerRefreshToken);

    if (!jwtUtil.verifyRefreshToken(refreshToken)) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtUtil.findRefreshTokenById(memberId);

    // 기존에 지정된 RefreshToken과 일치하지 않는 경우(재사용된 refreshToken인 경우)
    if (!refreshToken.equals(savedRefreshToken)) {
      // RefreshToken 삭제 및 다시 로그인하도록 UNAUTHORIZED
      jwtUtil.deleteRefreshToken(memberId);
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    return getJwtDto(member);
  }

  // 로그아웃
  public void logout(MemberInfoDto memberInfoDto, String bearerAccessToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    jwtUtil.setBlackList(accessToken); // 재사용 방지
    String memberId = String.valueOf(memberInfoDto.getMember().getId());

    // refreshToken 삭제
    if (jwtUtil.findRefreshTokenById(memberId) != null) {
      jwtUtil.deleteRefreshToken(memberId);
    }
  }

  // 회원 탈퇴
  @Transactional
  public void withdrawal(MemberInfoDto memberInfoDto, WithdrawalDto withdrawalType) {

    memberInfoDto.getMember().updateStatus(Status.INACTIVE);

    MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
        .member(memberInfoDto.getMember())
        .withdrawalType(WithdrawalType.valueOf(withdrawalType.getWithdrawalType()))
        .build();
    memberWithdrawalRepository.save(memberWithdrawal);
  }

  // 매일, 비활성화된 회원 중 3개월이 지난 회원 완전 탈퇴 처리
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void deleteWithdrawalMemberInfo() {
    List<Member> members = memberRepository.findInactiveMembersForWithdrawalDate();

    if (!members.isEmpty()) {
      members.forEach(Member::updatePersonalInfo);
    }
  }

  // 강제 회원 탈퇴 [테스트용]
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
