package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_WITHDRAWAL_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.entity.enums.WithdrawalType;
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

  /**
   * 회원 가입
   *
   * @param joinDto 회원 가입 정보
   * @param multipartFile 프로필 사진
   * @return JWT
   * @throws ConflictException provider, providerId로 이미 가입된 회원이 존재하는 경우
   */
  @Transactional
  public JwtDto join(MemberRequest.JoinDto joinDto, MultipartFile multipartFile) {
    Optional<Member> savedMember = memberRepository.findByProviderAndProviderId(
        Provider.valueOf(joinDto.getProvider()),
        joinDto.getProviderId());

    // 이미 가입한 경우
    if (savedMember.isPresent()) {
      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    String nickname = determineNickname(joinDto);
    validateNickname(nickname);
    ImageFile profileImageFile = createProfileImageFile(multipartFile);
    Member member = createMember(joinDto, profileImageFile, nickname);

    // GUEST가 아닌 경우, 이용약관 저장
    if (!member.getProvider().equals(Provider.GUEST)) {
      memberConsentService.createMemberConsents(member, joinDto.getConsentItems());
    }

    // fcm 토큰 저장
    if (joinDto.getFcmToken() != null) {
      fcmTokenService.createFcmToken(member, joinDto.getFcmToken());
    }

    return getJwtDto(member);
  }

  /**
   * GUEST 유형의 경우 UUID를 사용하여 랜덤 닉네임 생성
   * GUEST가 아닌 경우, 제공된 닉네임을 반환
   *
   * @param joinDto 회원 가입 정보
   * @return 생성된 닉네임
   */
  private String determineNickname(MemberRequest.JoinDto joinDto) {
    if (Provider.valueOf(joinDto.getProvider()) == Provider.GUEST) {
      return UUID.randomUUID().toString().substring(0, 12);
    }
    return joinDto.getNickname();
  }

  /**
   * 닉네임 중복 확인
   * 회원 가입 하기 전에 사용되는 메서드로, 본인 닉네임과 비교하지 않음
   *
   * @param nickname 닉네임
   * @throws ConflictException 닉네임이 중복되는 경우
   */
  public void validateNickname(String nickname) {
    Optional<Member> savedMember = memberRepository.findByNickname(nickname);
    if (savedMember.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  /**
   * 프로필 사진 업로드 및 저장
   * 프로필 사진이 없는 경우엔, 랜덤 프로필 사진 저장
   *
   * @param multipartFile 프로필 사진
   * @return 저장된 이미지 파일 또는 랜덤 프로필 사진 파일
   */
  private ImageFile createProfileImageFile(MultipartFile multipartFile) {
    if (multipartFile == null) {
      return imageFileService.getRandomProfileImageFile();
    }
    return imageFileService.uploadAndSaveImageFile(multipartFile, true);
  }

  /**
   * 회원 객체 생성 및 DB 저장
   *
   * @param joinDto 회원 가입 정보
   * @param imageFile 프로필 사진
   * @param nickname 닉네임
   * @return 저장된 회원
   */
  private Member createMember(MemberRequest.JoinDto joinDto, ImageFile imageFile, String nickname) {

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

  /**
   * 로그인
   * 회원의 상태(Status)가 INACTIVE라면, ACTIVE로 수정
   * 회원의 언어(Language)가 다르다면, 언어 수정
   * FcmToken이 없다면, 생성 및 timestamp 갱신
   *
   * @param loginDto 로그인 정보
   * @return JWT
   * @throws NotFoundException 존재하는 회원이 없는 경우
   */
  @Transactional
  public JwtDto login(MemberRequest.LoginDto loginDto) {

    Member member = memberRepository.findByProviderAndProviderId(
            Provider.valueOf(loginDto.getProvider()), loginDto.getProviderId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    updateMemberActive(member);
    updateLanguageDifferent(loginDto, member);

    // fcm 토큰이 없다면 생성, timestamp 갱신
    FcmToken fcmToken = fcmTokenService.getFcmToken(member, loginDto.getFcmToken());
    if (fcmToken == null && loginDto.getFcmToken() != null) {
      fcmToken = fcmTokenService.createFcmToken(member, loginDto.getFcmToken());
      fcmToken.updateTimestampToNow();
    }

    return getJwtDto(member);
  }

  /**
   * JWT 생성
   *
   * @param member 회원
   * @return JWT
   */
  private JwtDto getJwtDto(Member member) {
    String accessToken = jwtUtil.createAccessToken(String.valueOf(member.getId()),
        member.getRoleSet());
    String refreshToken = jwtUtil.createRefreshToken(String.valueOf(member.getId()),
        member.getRoleSet());

    return JwtDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * 회원 상태 재활성화 및 탈퇴 비활성화
   *
   * @param member 회원
   * @throws NotFoundException 존재하는 회원 탈퇴 정보가 없는 경우
   */
  @Transactional
  public void updateMemberActive(Member member) {
    if (member.getStatus().equals(Status.INACTIVE)) {
      member.updateStatus(Status.ACTIVE);

      MemberWithdrawal memberWithdrawal = memberWithdrawalRepository.findByMember(member)
          .orElseThrow(() -> new NotFoundException(MEMBER_WITHDRAWAL_NOT_FOUND.getMessage()));
      memberWithdrawal.updateStatus(Status.INACTIVE);
    }
  }

  /**
   * 언어 설정 변경
   *
   * @param loginDto 로그인 정보
   * @param member 회원
   */
  @Transactional
  public void updateLanguageDifferent(MemberRequest.LoginDto loginDto, Member member) {
    Language language = Language.valueOf(loginDto.getLocale());
    if (!member.getLanguage().equals(language)) {
      member.updateLanguage(language);
    }
  }

  /**
   * JWT 재발행
   *
   * @param bearerRefreshToken Bearer RefreshToken
   * @param fcmToken FcmToken
   * @return JWT
   * @throws UnauthorizedException 토큰이 유효하지 않은 경우
   * @throws NotFoundException 존재하는 회원이 없는 경우
   */
  @Transactional
  public JwtDto reissue(String bearerRefreshToken, String fcmToken) {
    String refreshToken = jwtUtil.resolveToken(bearerRefreshToken);

    if (!jwtUtil.verifyRefreshToken(refreshToken)) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtUtil.findRefreshToken(memberId);

    // 기존에 지정된 RefreshToken과 일치하지 않는 경우(재사용된 refreshToken인 경우)
    if (!refreshToken.equals(savedRefreshToken)) {
      // RefreshToken 삭제 및 다시 로그인하도록 UNAUTHORIZED
      jwtUtil.deleteRefreshToken(memberId);
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    // fcm 토큰이 없다면 생성, timestamp 갱신
    FcmToken fcmTokenInstance = fcmTokenService.getFcmToken(member, fcmToken);
    if (fcmTokenInstance == null && fcmToken != null) {
      fcmTokenInstance = fcmTokenService.createFcmToken(member, fcmToken);
      fcmTokenInstance.updateTimestampToNow();
    }

    return getJwtDto(member);
  }

  /**
   * 로그아웃
   * accessToken의 재사용 방지를 위해 블랙리스트에 추가한다.
   * RefreshToken과 FcmToken을 삭제한다.
   *
   * @param memberInfoDto 회원 정보
   * @param bearerAccessToken Bearer RefreshToken
   * @param fcmToken FcmToken
   */
  @Transactional
  public void logout(MemberInfoDto memberInfoDto, String bearerAccessToken, String fcmToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    jwtUtil.setBlackList(accessToken); // 재사용 방지
    String memberId = String.valueOf(memberInfoDto.getMember().getId());

    // refreshToken 삭제
    if (jwtUtil.findRefreshToken(memberId) != null) {
      jwtUtil.deleteRefreshToken(memberId);
    }

    // fcm 토큰 삭제
    FcmToken fcmTokenInstance = fcmTokenService.getFcmToken(memberInfoDto.getMember(), fcmToken);
    if (fcmTokenInstance != null) {
      fcmTokenService.deleteFcmToken(fcmTokenInstance);
    }
  }

  /**
   * 회원 탈퇴(비활성화)
   * 회원의 상태(Status)를 INACTIVE로 변환하고, 회원 탈퇴 정보를 저장한다.
   *
   * @param memberInfoDto 회원 정보
   * @param withdrawalDto 회원 탈퇴 요청 정보
   */
  @Transactional
  public void withdrawal(MemberInfoDto memberInfoDto, MemberRequest.WithdrawalDto withdrawalDto) {

    memberInfoDto.getMember().updateStatus(Status.INACTIVE);

    MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
        .member(memberInfoDto.getMember())
        .withdrawalType(WithdrawalType.valueOf(withdrawalDto.getWithdrawalType()))
        .build();
    memberWithdrawalRepository.save(memberWithdrawal);
  }

  /**
   * 매일 0시 0분 0초에 실행되는 회원 탈퇴 스케줄러
   * 비활성화 후 3개월이 지난 회원 탈퇴 처리
   */
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void deleteWithdrawalMemberInfo() {
    List<Member> members = memberRepository.findAllInactiveMember();

    if (!members.isEmpty()) {
      members.forEach(Member::updatePersonalInfo);
    }
  }

  /**
   * 강제 회원 탈퇴
   * 회원의 탈퇴일을 4개월 전으로 수정 후, 회원 탈퇴 스케줄러를 실행한다.
   *
   * @param bearerAccessToken Bearer AccessToken
   * @throws NotFoundException 존재하는 회원이 없거나, 존재하는 회원 탈퇴 정보가 없는 경우
   */
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
        .orElseThrow(() -> new NotFoundException(MEMBER_WITHDRAWAL_NOT_FOUND.getMessage()));
    memberWithdrawal.updateWithdrawalDate(); // 탈퇴일을 4개월 전으로 변경
    deleteWithdrawalMemberInfo();
  }
}
