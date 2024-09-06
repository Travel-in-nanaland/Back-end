package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberRequest.JoinDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import com.jeju.nanaland.domain.member.entity.enums.WithdrawalType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class MemberLoginServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private MemberConsentRepository memberConsentRepository;
  @Mock
  private MemberWithdrawalRepository memberWithdrawalRepository;
  @Mock
  private JwtUtil jwtUtil;
  @Mock
  private ImageFileService imageFileService;
  @Mock
  private MemberConsentService memberConsentService;
  @Mock
  private FcmTokenService fcmTokenService;
  @InjectMocks
  private MemberLoginService memberLoginService;

  private MemberRequest.JoinDto joinDto;
  private ImageFile imageFile;


  @BeforeEach
  void setUp() {
    joinDto = createJoinDto("GOOGLE");
    imageFile = createImageFile();
  }

  private MemberRequest.JoinDto createJoinDto(String provider) {
    MemberRequest.JoinDto joinDto = new MemberRequest.JoinDto();
    joinDto.setProvider(provider);
    joinDto.setProviderId("123");
    joinDto.setNickname("testNickname");
    joinDto.setEmail("test@example.com");
    joinDto.setLocale("KOREAN");
    joinDto.setGender("MALE");
    joinDto.setBirthDate(LocalDate.now());
    return joinDto;
  }

  private ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
  }

  private Member createMember(Language language, JoinDto joinDto) {
    return spy(Member.builder()
        .language(language)
        .email(joinDto.getEmail())
        .profileImageFile(imageFile)
        .nickname(joinDto.getNickname())
        .gender(joinDto.getGender())
        .birthDate(joinDto.getBirthDate())
        .provider(Provider.valueOf(joinDto.getProvider()))
        .providerId(joinDto.getProviderId())
        .travelType(TravelType.NONE)
        .build());
  }

  private MemberInfoDto createMemberInfoDto(Language language, Member member) {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  private MemberRequest.LoginDto createLoginDto(String locale) {
    MemberRequest.LoginDto loginDto = new MemberRequest.LoginDto();
    loginDto.setLocale(locale);
    loginDto.setProvider("GOOGLE");
    loginDto.setProviderId("123");
    loginDto.setFcmToken("test");
    return loginDto;
  }

  private MemberWithdrawal createMemberWithdrawal(Member member) {
    return MemberWithdrawal.builder()
        .withdrawalType(WithdrawalType.INCONVENIENT_COMMUNITY)
        .member(member)
        .build();
  }

  @Nested
  @DisplayName("회원 가입 TEST")
  class Join {
    @Test
    @DisplayName("실패 - 이미 회원 가입된 경우")
    void joinFail_memberDuplicate() {

      // given: 이미 가입된 회원이 존재하도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);

      doReturn(Optional.of(member))
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());

      // when: 회원 가입
      ConflictException conflictException = assertThrows(ConflictException.class,
          () -> memberLoginService.join(joinDto, null));

      // then: ErrorCode 검증
      assertThat(conflictException.getMessage()).isEqualTo(ErrorCode.MEMBER_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("실패 - 닉네임 중복")
    void joinFail_nicknameDuplicate() {

      // given: 이미 해당 닉네임을 사용 중인 회원이 존재하도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);

      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.of(member)).when(memberRepository).findByNickname(any());

      // when: 회원 가입
      ConflictException conflictException = assertThrows(ConflictException.class,
          () -> memberLoginService.join(joinDto, null));

      // then: ErrorCode 검증
      assertThat(conflictException.getMessage()).isEqualTo(ErrorCode.NICKNAME_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("성공 - GUEST가 아닌 경우")
    void joinSuccess_providerNotGUEST() {
      // given: provider가 GUEST가 아닌 회원 설정, 프로필 사진 없음
      Language language = Language.KOREAN;
      Member member = createMember(language, createJoinDto("GOOGLE"));

      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
      doReturn(imageFile).when(imageFileService).getRandomProfileImageFile();
      doReturn(member).when(memberRepository).save(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());

      // when: 회원 가입
      JwtDto result = memberLoginService.join(joinDto, null);

      // then: JWT 생성 확인, 이용약관 생성 확인, 프로필 사진 확인
      assertThat(result).isNotNull();
      assertThat(result.getAccessToken()).isEqualTo("accessToken");
      assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
      verify(memberConsentService).createMemberConsents(any(), any());
      verify(memberRepository).save(argThat(savedMember ->
          savedMember.getProfileImageFile().equals(imageFile)));
    }

    @Test
    @DisplayName("성공 - GUEST인 경우")
    void joinSuccess_providerGUEST() {
      // given: provider가 GUEST인 회원 설정, 프로필 사진 없음
      Language language = Language.KOREAN;
      Member member = createMember(language, createJoinDto("GUEST"));

      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
      doReturn(imageFile).when(imageFileService).getRandomProfileImageFile();
      doReturn(member).when(memberRepository).save(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());

      // when: 회원 가입
      JwtDto result = memberLoginService.join(joinDto, null);

      // then: JWT 생성 확인, 이용약관 생성이 진행되지 않음 확인, 프로필 사진 확인
      assertThat(result).isNotNull();
      assertThat(result.getAccessToken()).isEqualTo("accessToken");
      assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
      verify(memberConsentService, never()).createMemberConsents(any(), any());
      verify(memberRepository).save(argThat(savedMember ->
          savedMember.getProfileImageFile().equals(imageFile)));
    }

    @Test
    @DisplayName("성공 - 프로필 사진이 있는 경우")
    void joinSuccess_multipartFileExists() {
      // given: 프로필 사진이 있는 경우
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
          new byte[0]);
      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
      doReturn(imageFile).when(imageFileService).uploadAndSaveImageFile(any(), anyBoolean());
      doReturn(member).when(memberRepository).save(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());

      // when: 회원 가입
      JwtDto result = memberLoginService.join(joinDto, multipartFile);

      // then: JWT 생성 확인, 이용약관 생성 확인, 프로필 사진 확인
      assertThat(result).isNotNull();
      assertThat(result.getAccessToken()).isEqualTo("accessToken");
      assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
      verify(memberConsentService).createMemberConsents(any(), any());
      verify(memberRepository).save(argThat(savedMember ->
          savedMember.getProfileImageFile().equals(imageFile)));
    }

    @Test
    @DisplayName("성공 - fcmToken이 존재하는 경우")
    void joinSuccess_fcmTokenExists(){
      // given: fcmToken을 회원 가입 요청 DTO에 추가로 설정
      Language language = Language.KOREAN;
      JoinDto joinDto2 = createJoinDto("GOOGLE");
      joinDto2.setFcmToken("fcmToken");
      Member member = createMember(language, joinDto2);
      MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
          new byte[0]);
      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
      doReturn(imageFile).when(imageFileService).uploadAndSaveImageFile(any(), anyBoolean());
      doReturn(member).when(memberRepository).save(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());

      // when: 회원 가입
      JwtDto result = memberLoginService.join(joinDto2, multipartFile);

      // then: JWT 생성 확인, 이용약관 생성 확인, fcmToken 생성 확인, 프로필 사진 확인
      assertThat(result).isNotNull();
      assertThat(result.getAccessToken()).isEqualTo("accessToken");
      assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
      verify(memberConsentService).createMemberConsents(any(), any());
      verify(fcmTokenService).createFcmToken(any(), any());
      verify(memberRepository).save(argThat(savedMember ->
          savedMember.getProfileImageFile().equals(imageFile)));
    }
  }

  @Nested
  @DisplayName("로그인 TEST")
  class Login {
    @Test
    @DisplayName("실패 - 회원 가입이 안된 경우")
    void loginFail_memberNotFound() {
      // given: 로그인 요청 DTO 설정, 가입된 회원이 존재하지 않도록 설정
      MemberRequest.LoginDto loginDto = createLoginDto(Language.KOREAN.name());

      doReturn(Optional.empty())
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());

      // when: 로그인
      NotFoundException notFoundException = assertThrows(NotFoundException.class,
          () -> memberLoginService.login(loginDto));

      // then: ErrorCode 검증
      assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void loginSuccess() {
      // given: 로그인 요청 DTO 설정, fcmToken이 존재하도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      MemberRequest.LoginDto loginDto = createLoginDto(Language.KOREAN.name());

      doReturn(Optional.of(member))
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(FcmToken.builder().build())
          .when(fcmTokenService).getFcmToken(any(Member.class), any(String.class));

      // when: 로그인
      JwtDto jwtDto = memberLoginService.login(loginDto);

      // then: JWT 생성 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("성공 - 비활성화 회원이 로그인한 경우")
    void loginSuccess_inactiveMember() {
      // given: INACTIVE 상태의 회원의 로그인 요청 DTO 설정, fcmToken이 존재하도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      member.updateStatus(Status.INACTIVE);
      MemberRequest.LoginDto loginDto = createLoginDto(Language.KOREAN.name());
      MemberWithdrawal memberWithdrawal = createMemberWithdrawal(member);

      doReturn(Optional.of(member))
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn(Optional.of(memberWithdrawal)).when(memberWithdrawalRepository).findByMember(member);
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(FcmToken.builder().build())
          .when(fcmTokenService).getFcmToken(any(Member.class), any(String.class));

      // when: 로그인
      JwtDto jwtDto = memberLoginService.login(loginDto);

      // then: JWT 생성 확인, 회원의 상태가 ACTIVE인지 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
      assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("성공 - 언어 설정을 변경한 경우")
    void loginSuccess_updateLanguage() {
      // given: 언어 설정을 변경한 회원의 로그인 요청 DTO 설정, fcmToken이 존재하도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      MemberRequest.LoginDto loginDto = createLoginDto(Language.ENGLISH.name());

      doReturn(Optional.of(member))
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(FcmToken.builder().build())
          .when(fcmTokenService).getFcmToken(any(Member.class), any(String.class));

      // when: 로그인
      JwtDto jwtDto = memberLoginService.login(loginDto);

      // then: JWT 생성 확인, 언어 설정 변경 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
      assertThat(member.getLanguage()).isEqualTo(Language.ENGLISH);
    }

    @Test
    @DisplayName("성공 - fcmToken이 존재하지 않는 경우")
    void loginSuccess_fcmTokenNotExists() {
      // given: 언어 설정을 변경한 회원의 로그인 요청 DTO 설정, fcmToken이 존재하지 않도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      MemberRequest.LoginDto loginDto = createLoginDto(Language.KOREAN.name());

      doReturn(Optional.of(member))
          .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(null)
          .when(fcmTokenService).getFcmToken(any(Member.class), any(String.class));
      doReturn(FcmToken.builder().build())
          .when(fcmTokenService).createFcmToken(any(Member.class), any(String.class));

      // when: 로그인
      JwtDto jwtDto = memberLoginService.login(loginDto);

      // then: JWT 생성 확인, 언어 설정 변경 확인, fcmToken 생성 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
      verify(fcmTokenService).createFcmToken(any(), any());
    }
  }

  @Nested
  @DisplayName("회원 상태 재활성화 및 탈퇴 비활성화 TEST")
  class UpdateMemberActive {

    @Test
    @DisplayName("실패 - memberWithdrawal를 찾을 수 없는 경우")
    void updateMemberActiveFail_memberWithdrawalNotFound() {
      // given: INACTIVE 회원 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      member.updateStatus(Status.INACTIVE);
      doReturn(Optional.empty()).when(memberWithdrawalRepository).findByMember(member);

      // when: 회원 상태 재활성화 및 탈퇴 비활성화
      NotFoundException notFoundException = assertThrows(NotFoundException.class,
          () -> memberLoginService.updateMemberActive(member));

      // then: ErrorCode 검증
      assertThat(notFoundException.getMessage()).isEqualTo(
          ErrorCode.MEMBER_WITHDRAWAL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void updateMemberActiveSuccess() {
      // given: INACTIVE 회원 설정, 회원 탈퇴 정보 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);
      member.updateStatus(Status.INACTIVE);
      MemberWithdrawal memberWithdrawal = createMemberWithdrawal(member);
      doReturn(Optional.of(memberWithdrawal)).when(memberWithdrawalRepository).findByMember(member);

      // when: 회원 상태 재활성화 및 탈퇴 비활성화
      memberLoginService.updateMemberActive(member);

      // then: 회원 상태 ACTIVE 확인, 탈퇴 정보 INACTIVE 확인
      assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(memberWithdrawal.getStatus()).isEqualTo(Status.INACTIVE);
    }
  }

  @Test
  @DisplayName("언어 설정 변경 성공 TEST")
  void updateLanguageDifferent() {
    // given: 언어 설정이 변경된 회원 설정
    Language language = Language.KOREAN;
    Member member = createMember(language, joinDto);
    MemberRequest.LoginDto loginDto = createLoginDto(Language.ENGLISH.name());

    // when: 언어 설정 변경
    memberLoginService.updateLanguageDifferent(loginDto, member);

    // then: 언어 변경 확인
    assertThat(member.getLanguage()).isEqualTo(Language.ENGLISH);
  }

  @Nested
  @DisplayName("JWT 재발행 TEST")
  class Reissue {
    @Test
    @DisplayName("실패 - 유효하지 않은 토큰인 경우")
    void reissueFail_invalidToken() {
      // given: 유효하지 않은 토큰 설정
      doReturn("refreshToken").when(jwtUtil).resolveToken(any());
      doReturn(false).when(jwtUtil).verifyRefreshToken(any());

      // when: JWT 재발행
      UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
          () -> memberLoginService.reissue("bearer RefreshToken", ""));

      // then: ErrorCode 검증
      assertThat(unauthorizedException.getMessage()).isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("실패 - 저장된 토큰과 다른 경우")
    void reissueFail_tokenDifferent() {
      // given: 유효하지만 저장된 토큰과 다른 토큰 설정
      doReturn("refreshToken").when(jwtUtil).resolveToken(any());
      doReturn(true).when(jwtUtil).verifyRefreshToken(any());
      doReturn("1").when(jwtUtil).getMemberIdFromRefresh(any());
      doReturn("refreshToken2").when(jwtUtil).findRefreshToken(any());

      // when: JWT 재발행
      UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
          () -> memberLoginService.reissue("bearer RefreshToken", ""));

      // then: ErrorCode 검증, RefreshToken 삭제 확인
      assertThat(unauthorizedException.getMessage()).isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
      verify(jwtUtil).deleteRefreshToken(any());
    }

    @Test
    @DisplayName("성공")
    void reissueSuccess() {
      // given: 유효하고 저장된 토큰과 같은 토큰 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);

      doReturn("refreshToken").when(jwtUtil).resolveToken(any());
      doReturn(true).when(jwtUtil).verifyRefreshToken(any());
      doReturn("1").when(jwtUtil).getMemberIdFromRefresh(any());
      doReturn("refreshToken").when(jwtUtil).findRefreshToken(any());

      doReturn(Optional.of(member)).when(memberRepository).findById(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(FcmToken.builder().build()).when(fcmTokenService).getFcmToken(any(), any());

      // when: JWT 재발행
      JwtDto jwtDto = memberLoginService.reissue("bearer RefreshToken", "token");

      // then: JWT 생성 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("성공 - fcmToken이 존재하지 않는 경우")
    void reissueSuccess_fcmTokenNotExists() {
      // given: 유효하고 저장된 토큰과 같은 토큰 설정, fcmToken이 존재하지 않도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, joinDto);

      doReturn("refreshToken").when(jwtUtil).resolveToken(any());
      doReturn(true).when(jwtUtil).verifyRefreshToken(any());
      doReturn("1").when(jwtUtil).getMemberIdFromRefresh(any());
      doReturn("refreshToken").when(jwtUtil).findRefreshToken(any());

      doReturn(Optional.of(member)).when(memberRepository).findById(any());
      doReturn("accessToken").when(jwtUtil).createAccessToken(any(), any());
      doReturn("refreshToken").when(jwtUtil).createRefreshToken(any(), any());
      doReturn(1L).when(member).getId();
      doReturn(null).when(fcmTokenService).getFcmToken(any(), any());
      doReturn(FcmToken.builder().build()).when(fcmTokenService).createFcmToken(any(), any());

      // when: JWT 재발행
      JwtDto jwtDto = memberLoginService.reissue("bearer RefreshToken", "token");

      // then: JWT 생성 확인, fcmToken 생성 확인
      assertThat(jwtDto).isNotNull();
      assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
      assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
      verify(fcmTokenService).createFcmToken(any(), any());
    }
  }

  @Test
  @DisplayName("로그아웃 성공 TEST")
  void logoutSuccess() {
    // given: 유효한 AccessToken 설정, 저장된 RefreshToken가 존재하도록 설정, fcmToken이 존재하도록 설정
    Language language = Language.KOREAN;
    Member member = createMember(language, joinDto);
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

    doReturn("accessToken").when(jwtUtil).resolveToken(any());
    doReturn("refreshToken").when(jwtUtil).findRefreshToken(any());
    doReturn(FcmToken.builder().build()).when(fcmTokenService).getFcmToken(any(), any());

    // when: 로그아웃
    memberLoginService.logout(memberInfoDto, "bearer refreshToken", "token");

    // then: AccessToken 블랙리스트 지정 확인, RefreshToken과 fcmToken 삭제 확인
    verify(jwtUtil).setBlackList(any());
    verify(jwtUtil).deleteRefreshToken(any());
    verify(fcmTokenService).deleteFcmToken(any());
  }

  @Test
  @DisplayName("회원 탈퇴(비활성화) 성공 TEST")
  void withdrawal() {
    // given: 회원 탈퇴 요청 DTOO 설정
    Language language = Language.KOREAN;
    Member member = createMember(language, joinDto);
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    MemberRequest.WithdrawalDto withdrawalDto = new MemberRequest.WithdrawalDto();
    withdrawalDto.setWithdrawalType(WithdrawalType.INCONVENIENT_SERVICE.name());

    // when: 회원 탈퇴
    memberLoginService.withdrawal(memberInfoDto, withdrawalDto);

    // then: 회원의 상태가 INACTIVE인지 확인, 회원 탈퇴 저장 확인
    assertThat(member.getStatus()).isEqualTo(Status.INACTIVE);
    verify(member).updateStatus(any());
    verify(memberWithdrawalRepository).save(any());
  }

  @Test
  @DisplayName("회원 탈퇴 스케줄러 TEST")
  void deleteWithdrawalMemberInfo() {
    // given: 비활성화된 회원 설정
    Language language = Language.KOREAN;
    Member member = createMember(language, joinDto);

    doReturn(List.of(member)).when(memberRepository).findAllInactiveMember();

    // when: 회원 탈퇴
    memberLoginService.deleteWithdrawalMemberInfo();

    // then: 회원 개인정보 삭제 확인
    assertThat(member.getEmail()).isEqualTo("INACTIVE@nanaland.com");
    assertThat(member.getProviderId()).isEqualTo("INACTIVE");
    assertThat(member.getGender()).isEmpty();
    assertThat(member.getBirthDate()).isNull();
    verify(member).updatePersonalInfo();
  }
}