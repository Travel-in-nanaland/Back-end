package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ProfileUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.member.repository.MemberWithdrawalRepository;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import com.jeju.nanaland.global.util.JwtUtil;
import java.io.IOException;
import java.time.LocalDate;
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
class MemberProfileServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private LanguageRepository languageRepository;
  @Mock
  private MemberTravelTypeRepository memberTravelTypeRepository;
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
  private S3ImageService s3ImageService;
  @InjectMocks
  private MemberProfileService memberProfileService;

  private ProfileUpdateDto profileUpdateDto;
  private MemberTravelType memberTravelType;
  private ImageFile imageFile;

  @BeforeEach
  void setUp() {
    profileUpdateDto = createProfileUpdateDto();
    memberTravelType = createMemberTravelType();
    imageFile = createImageFile();
  }

  private Language createLanguage(Locale locale) {
    return Language.builder()
        .locale(locale)
        .dateFormat("yy-MM-dd")
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

  private Member createMember(Language language) {
    return spy(Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .memberTravelType(memberTravelType)
        .build());
  }

  private MemberInfoDto createMemberInfoDto(Language language, Member member) {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  private ProfileUpdateDto createProfileUpdateDto() {
    profileUpdateDto = new ProfileUpdateDto();
    profileUpdateDto.setNickname("updateNickname");
    profileUpdateDto.setDescription("updateDescription");
    return profileUpdateDto;
  }

  @Test
  @DisplayName("프로필 수정 실패 - 닉네임이 중복되는 경우")
  void updateProfileFail() {
    // given
    Language language = createLanguage(Locale.KOREAN);
    Member member = createMember(language);
    Member member2 = createMember(language);
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        new byte[0]);

    doReturn(Optional.of(member2)).when(memberRepository).findByNickname(any());

    // when

    ConflictException conflictException = assertThrows(ConflictException.class,
        () -> memberProfileService.updateProfile(memberInfoDto, profileUpdateDto, multipartFile));

    // then
    assertThat(conflictException.getMessage()).isEqualTo(ErrorCode.NICKNAME_DUPLICATE.getMessage());
  }

  @Test
  @DisplayName("프로필 수정 성공")
  void updateProfileSuccess() throws IOException {
    // given
    Language language = createLanguage(Locale.KOREAN);
    Member member = createMember(language);
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        new byte[0]);
    S3ImageDto s3ImageDto = S3ImageDto.builder()
        .originUrl("updateOriginUrl")
        .thumbnailUrl("updateThumbnailUrl")
        .build();

    doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
    doReturn(s3ImageDto).when(s3ImageService).uploadOriginImageToS3(multipartFile, true);

    // when
    memberProfileService.updateProfile(memberInfoDto, profileUpdateDto, multipartFile);

    // then
    assertThat(member.getProfileImageFile().getOriginUrl()).isEqualTo(s3ImageDto.getOriginUrl());
    assertThat(member.getProfileImageFile().getThumbnailUrl()).isEqualTo(
        s3ImageDto.getThumbnailUrl());
    assertThat(member.getNickname()).isEqualTo(profileUpdateDto.getNickname());
    assertThat(member.getDescription()).isEqualTo(profileUpdateDto.getDescription());

    verify(memberRepository, times(1)).findByNickname(any());
  }

  @Test
  void getMemberProfile() {
  }

  @Test
  void updateLanguage() {
  }
}