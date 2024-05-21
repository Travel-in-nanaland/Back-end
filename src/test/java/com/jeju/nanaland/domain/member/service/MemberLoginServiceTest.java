package com.jeju.nanaland.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.util.JwtUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
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

  /**
   * 회원 가입 성공 조건 - DB에 Provider, providerId가 없어야 함 - 닉네임이 12자 내여야 함 & DB에 존재하지 않아야 함 -
   * MultipartFile이 null이면 기본 프로필 사진, 그렇지 않으면 upload - language & memberTravelType이 null이 아니여야 함 -
   * 이용 약관 저장 - JWT 저장
   */
  @Test
  @DisplayName("회원 가입 성공")
  void joinSuccess() {
    // given
    JoinDto joinDto = new JoinDto();
    joinDto.setProvider("GOOGLE");
    joinDto.setProviderId("123");
    joinDto.setNickname("testNickname");
    joinDto.setEmail("test@example.com");
    joinDto.setLocale("KOREAN");
    joinDto.setGender("MALE");
    joinDto.setBirthDate(LocalDate.now());
    joinDto.setConsentItems(Collections.emptyList());

    MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        new byte[0]);

    Language language = Language.builder()
        .locale(Locale.KOREAN)
        .build();
    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(TravelType.NONE)
        .build();
    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();

    Member member = spy(Member.builder()
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
  void login() {
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