package com.jeju.nanaland.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.JoinDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.util.JwtUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


@ExtendWith(MockitoExtension.class)
class MemberLoginServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private LanguageRepository languageRepository;
  @Mock
  private MemberTravelTypeRepository memberTravelTypeRepository;
  @Mock
  private MemberConsentRepository memberConsentRepository;
  @Mock
  private JwtUtil jwtUtil;
  @Mock
  private ImageFileService imageFileService;
  @Mock
  private MemberConsentService memberConsentService;
  @InjectMocks
  private MemberLoginService memberLoginService;

  private JoinDto joinDto;
  private Language language;
  private MemberTravelType memberTravelType;
  private ImageFile imageFile;
  private Member member;
  private LoginDto loginDto;


  @BeforeEach
  void setUp() {
    joinDto = createJoinDto();
    language = createLanguage(Locale.KOREAN);
    memberTravelType = createMemberTravelType();
    imageFile = createImageFile();
    member = createMember();
    loginDto = createLoginDto(Locale.KOREAN.name());
  }

  private JoinDto createJoinDto() {
    JoinDto joinDto = new JoinDto();
    joinDto.setProvider("GOOGLE");
    joinDto.setProviderId("123");
    joinDto.setNickname("testNickname");
    joinDto.setEmail("test@example.com");
    joinDto.setLocale("KOREAN");
    joinDto.setGender("MALE");
    joinDto.setBirthDate(LocalDate.now());
    joinDto.setConsentItems(Collections.emptyList());
    return joinDto;
  }

  private Language createLanguage(Locale locale) {
    return Language.builder()
        .locale(locale)
        .build();
  }

  private MemberTravelType createMemberTravelType() {
    return MemberTravelType.builder()
        .travelType(TravelType.NONE)
        .build();
  }

  private ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
  }

  private Member createMember() {
    return spy(Member.builder()
        .language(language)
        .email(joinDto.getEmail())
        .profileImageFile(imageFile)
        .nickname(joinDto.getNickname())
        .gender(joinDto.getGender())
        .birthDate(joinDto.getBirthDate())
        .provider(Provider.valueOf(joinDto.getProvider()))
        .providerId(joinDto.getProviderId())
        .memberTravelType(memberTravelType)
        .build());
  }

  private LoginDto createLoginDto(String locale) {
    LoginDto loginDto = new LoginDto();
    loginDto.setLocale(locale);
    loginDto.setProvider("GOOGLE");
    loginDto.setProviderId("123");
    return loginDto;
  }

  /**
   * 회원 가입 실패 조건 - 이미 회원 가입된 경우 - 닉네임 중복
   */
  @Test
  @DisplayName("회원 가입 실패 - 이미 회원 가입된 경우")
  void joinFail() {
    // given
    doReturn(Optional.of(member))
        .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());

    // when
    ConflictException conflictException = assertThrows(ConflictException.class,
        () -> memberLoginService.join(joinDto, null));

    // then
    assertEquals(conflictException.getMessage(), ErrorCode.MEMBER_DUPLICATE.getMessage());
  }

  @Test
  @DisplayName("회원 가입 실패 - 닉네임 중복")
  void joinFail2() {
    // given
    doReturn(Optional.empty())
        .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
    doReturn(Optional.of(member)).when(memberRepository).findByNickname(any());

    // when
    ConflictException conflictException = assertThrows(ConflictException.class,
        () -> memberLoginService.join(joinDto, null));

    // then
    assertEquals(conflictException.getMessage(), ErrorCode.NICKNAME_DUPLICATE.getMessage());
  }

  @Test
  @DisplayName("회원 가입 성공")
  void joinSuccess() {
    // given
    MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        new byte[0]);

    doReturn(Optional.empty())
        .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
    doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
    doReturn(imageFile).when(imageFileService).getRandomProfileImageFile();
    doReturn(imageFile).when(imageFileService).uploadAndSaveImageFile(any(), anyBoolean());
    doReturn(language).when(languageRepository).findByLocale(any(Locale.class));
    doReturn(memberTravelType)
        .when(memberTravelTypeRepository).findByTravelType(any(TravelType.class));
    doReturn(member).when(memberRepository).save(any());
    doReturn("accessToken").when(jwtUtil).getAccessToken(any(), any());
    doReturn("refreshToken").when(jwtUtil).getRefreshToken(any(), any());
    doReturn(1L).when(member).getId();

    // when
    JwtDto jwtDto = memberLoginService.join(joinDto, multipartFile);
    JwtDto jwtDto2 = memberLoginService.join(joinDto, null);

    // then
    assertNotNull(jwtDto);
    assertEquals("accessToken", jwtDto.getAccessToken());
    assertEquals("refreshToken", jwtDto.getRefreshToken());

    assertNotNull(jwtDto2);
    assertEquals("accessToken", jwtDto2.getAccessToken());
    assertEquals("refreshToken", jwtDto2.getRefreshToken());

    verify(memberRepository, times(2)).findByProviderAndProviderId(any(Provider.class), any());
    verify(memberRepository, times(2)).findByNickname(any());
    verify(imageFileService, times(1)).getRandomProfileImageFile();
    verify(imageFileService, times(1)).uploadAndSaveImageFile(any(), anyBoolean());
    verify(languageRepository, times(2)).findByLocale(any(Locale.class));
    verify(memberTravelTypeRepository, times(2)).findByTravelType(any(TravelType.class));
    verify(memberRepository, times(2)).save(any());
    verify(jwtUtil, times(2)).getAccessToken(any(), any());
    verify(jwtUtil, times(2)).getRefreshToken(any(), any());
    verify(member, times(4)).getId();
  }

  @Test
  @DisplayName("로그인 실패 - 회원 가입이 안된 경우")
  void loginFail() {
    // given
    doReturn(Optional.empty())
        .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> memberLoginService.login(loginDto));

    // then
    assertEquals(notFoundException.getMessage(), ErrorCode.MEMBER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("로그인 성공")
  void loginSuccess() {
    // given
    doReturn(Optional.of(member))
        .when(memberRepository).findByProviderAndProviderId(any(Provider.class), any());
    doReturn("accessToken").when(jwtUtil).getAccessToken(any(), any());
    doReturn("refreshToken").when(jwtUtil).getRefreshToken(any(), any());
    doReturn(1L).when(member).getId();

    // when
    JwtDto jwtDto = memberLoginService.login(loginDto);

    // then
    assertNotNull(jwtDto);
    assertEquals("accessToken", jwtDto.getAccessToken());
    assertEquals("refreshToken", jwtDto.getRefreshToken());
    verify(memberRepository, times(1)).findByProviderAndProviderId(any(Provider.class), any());
    verify(jwtUtil, times(1)).getAccessToken(any(), any());
    verify(jwtUtil, times(1)).getRefreshToken(any(), any());
    verify(member, times(2)).getId();
  }

  @Test
  void updateMemberActive() {
  }

  @Test
  void updateLanguageDifferent() {
  }

  @Test
  void reissue() {
  }

  @Test
  void logout() {
  }

  @Test
  void withdrawal() {
  }

  @Test
  void deleteWithdrawalMemberInfo() {
  }
}