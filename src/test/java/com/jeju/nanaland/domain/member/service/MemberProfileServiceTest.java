package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LanguageUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ProfileUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.ProfileDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class MemberProfileServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private S3ImageService s3ImageService;
  @InjectMocks
  private MemberProfileService memberProfileService;

  private ProfileUpdateDto profileUpdateDto;
  private ImageFile imageFile;

  @BeforeEach
  void setUp() {
    profileUpdateDto = createProfileUpdateDto();
    imageFile = createImageFile();
  }

  private ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
  }

  private Member createMember(Language language, String nickname) {
    return spy(Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname(nickname)
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.GAMGYUL)
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

  private MemberConsent createMemberConsent(ConsentType consentType, Member member) {
    return MemberConsent.builder()
        .consentType(consentType)
        .consent(true)
        .member(member)
        .build();
  }

  @Test
  @DisplayName("프로필 수정 실패 - 닉네임이 중복되는 경우")
  void updateProfileFail() {
    // given
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    Member member2 = createMember(language, "nickname");
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
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        new byte[0]);
    S3ImageDto s3ImageDto = S3ImageDto.builder()
        .originUrl("updateOriginUrl")
        .thumbnailUrl("updateThumbnailUrl")
        .build();

    doReturn(Optional.empty()).when(memberRepository).findByNickname(any());
    doReturn(s3ImageDto).when(s3ImageService)
        .uploadImageToS3(any(MultipartFile.class), eq(true), any());

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
  @DisplayName("유저 프로필 조회 실패 - 존재하지 않는 회원인 경우")
  void getMemberProfileFail() {
    // given
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

    doReturn(1L).when(member).getId();

    // when

    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> memberProfileService.getMemberProfile(memberInfoDto, 2L));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("내 프로필 조회 성공")
  void getMemberProfileSuccess() {
    // given
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    List<MemberConsent> memberConsents = List.of(
        createMemberConsent(ConsentType.MARKETING, member),
        createMemberConsent(ConsentType.LOCATION_SERVICE, member)
    );
    doReturn(1L).when(member).getId();
    doReturn(memberConsents).when(memberRepository).findAllMemberConsent(member);

    // when
    ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, null);
    ProfileDto profileDto2 = memberProfileService.getMemberProfile(memberInfoDto, 1L);

    // then
    assertThat(profileDto.getConsentItems()).hasSize(2);
    assertThat(profileDto.getEmail()).isEqualTo(member.getEmail());
    assertThat(profileDto.getProvider()).isEqualTo(member.getProvider().name());
    assertThat(profileDto.getProfileImage().getThumbnailUrl()).isEqualTo(
        member.getProfileImageFile().getThumbnailUrl());
    assertThat(profileDto.getNickname()).isEqualTo(member.getNickname());
    assertThat(profileDto.getDescription()).isEqualTo(member.getDescription());
    assertThat(profileDto.getTravelType()).isEqualTo(
        member.getTravelType().getTypeNameWithLocale(language));
    assertThat(profileDto.getHashtags()).hasSize(3);
    assertThat(profileDto2.getConsentItems()).hasSize(2);
    assertThat(profileDto2.getEmail()).isEqualTo(member.getEmail());
    assertThat(profileDto2.getProvider()).isEqualTo(member.getProvider().name());
    assertThat(profileDto2.getProfileImage().getThumbnailUrl()).isEqualTo(
        member.getProfileImageFile().getThumbnailUrl());
    assertThat(profileDto2.getNickname()).isEqualTo(member.getNickname());
    assertThat(profileDto2.getDescription()).isEqualTo(member.getDescription());
    assertThat(profileDto2.getTravelType()).isEqualTo(
        member.getTravelType().getTypeNameWithLocale(language));
    assertThat(profileDto2.getHashtags()).hasSize(3);

    verify(memberRepository, times(2)).findAllMemberConsent(any());
  }

  @Test
  @DisplayName("타인 프로필 조회 성공")
  void getMemberProfileSuccess2() {
    // given
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    Language language2 = Language.ENGLISH;
    Member member2 = createMember(language2, "nickname2");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

    doReturn(1L).when(member).getId();
    doReturn(Optional.of(member2)).when(memberRepository).findById(2L);

    // when
    ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, 2L);

    // then
    assertThat(profileDto.getConsentItems()).isEmpty();
    assertThat(profileDto.getEmail()).isEqualTo(member2.getEmail());
    assertThat(profileDto.getProvider()).isEqualTo(member2.getProvider().name());
    assertThat(profileDto.getProfileImage().getThumbnailUrl()).isEqualTo(
        member.getProfileImageFile().getThumbnailUrl());
    assertThat(profileDto.getNickname()).isEqualTo(member2.getNickname());
    assertThat(profileDto.getDescription()).isEqualTo(member2.getDescription());
    assertThat(profileDto.getTravelType()).isEqualTo(
        member2.getTravelType().getTypeNameWithLocale(language2));
    assertThat(profileDto.getHashtags()).hasSize(3);

    verify(memberRepository, times(0)).findAllMemberConsent(any());
  }

  @Test
  @DisplayName("언어 변경")
  void updateLanguage() {
    // given
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    Language language2 = Language.ENGLISH;
    LanguageUpdateDto languageUpdateDto = new LanguageUpdateDto();
    languageUpdateDto.setLocale(Language.ENGLISH.name());

    // when
    memberProfileService.updateLanguage(memberInfoDto, languageUpdateDto);

    // then
    assertThat(member.getLanguage()).isEqualTo(language2);
  }
}