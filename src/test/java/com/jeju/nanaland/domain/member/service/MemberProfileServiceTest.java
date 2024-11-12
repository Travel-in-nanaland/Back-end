package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.file.service.FileUploadService;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class MemberProfileServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private S3ImageService s3ImageService;
  @Mock
  private ProfileImageService profileImageService;
  @InjectMocks
  private MemberProfileService memberProfileService;
  @Mock
  private FileUploadService fileUploadService;

  private MemberRequest.ProfileUpdateDto profileUpdateDto;
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

  private MemberRequest.ProfileUpdateDto createProfileUpdateDto() {
    profileUpdateDto = new MemberRequest.ProfileUpdateDto();
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

  @Nested
  @DisplayName("유저 프로필 수정 TEST")
  class UpdateProfile {
    @Test
    @DisplayName("실패 - 닉네임이 중복되는 경우")
    void updateProfileFail_nicknameDuplicate() {
      // given: 이미 해당 닉네임을 사용 중인 회원이 존재하도록 프로필 수정 요청 DTO 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      Member member2 = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

      doReturn(Optional.of(member2)).when(memberRepository).findByNickname(any(String.class));

      // when: 유저 프로필 수정
      ConflictException conflictException = assertThrows(ConflictException.class,
          () -> memberProfileService.updateProfile(memberInfoDto, profileUpdateDto, null));

      // then: ErrorCode 검증
      assertThat(conflictException.getMessage()).isEqualTo(ErrorCode.NICKNAME_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("성공 - 이미지 변경 없는 경우")
    void updateProfileSuccess_multipartFileNotExists() {
      // given: 닉네임이 유효하고, 프로필 사진 있도록 프로필 수정 요청 DTO 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

      doReturn(Optional.empty()).when(memberRepository).findByNickname(any(String.class));

      // when: 유저 프로필 수정
      memberProfileService.updateProfile(memberInfoDto, profileUpdateDto, null);

      // then: 프로필 수정 확인, 이미지 변경 없음 확인
      assertThat(member.getNickname()).isEqualTo(profileUpdateDto.getNickname());
      assertThat(member.getDescription()).isEqualTo(profileUpdateDto.getDescription());
      verify(s3ImageService, never()).uploadImageToS3(any(MultipartFile.class), anyBoolean(), any(String.class));
    }

    @Test
    @DisplayName("성공 - 새 이미지 업로드")
    void updateProfileSuccess_multipartFileExists() {
      // given: 닉네임이 유효하고, 새 이미지를 추가하여 프로필 수정 요청 DTO 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
      S3ImageDto s3ImageDto = S3ImageDto.builder()
          .originUrl("orignUrl")
          .thumbnailUrl("thumbnailUrl")
          .build();
      doReturn(Optional.empty()).when(memberRepository).findByNickname(any(String.class));
      doReturn(s3ImageDto).when(fileUploadService).getS3ImageUrls(any());

      // when: 유저 프로필 수정
      memberProfileService.updateProfile(memberInfoDto, profileUpdateDto, "test/abc_test.jpg");

      // then: 프로필 수정 확인, 이미지 변경 확인
      assertThat(member.getNickname()).isEqualTo(profileUpdateDto.getNickname());
      assertThat(member.getDescription()).isEqualTo(profileUpdateDto.getDescription());
      verify(fileUploadService).getS3ImageUrls(any());
    }
  }

  @Nested
  @DisplayName("유저 프로필 조회 TEST")
  class GetMemberProfile {

    @Test
    @DisplayName("실패 - 존재하지 않는 회원인 경우")
    void getMemberProfileFail_memberNotFound() {
      // given: 회원이 존재하지 않도록 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

      doReturn(1L).when(member).getId();

      // when: 유저 프로필 조회
      NotFoundException notFoundException = assertThrows(NotFoundException.class,
          () -> memberProfileService.getMemberProfile(memberInfoDto, 2L));

      // then: ErrorCode 검증
      assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공 - memberId가 null인 경우 내 프로필 조회")
    void getMemberProfileSuccess_memberIdNull() {
      // given: 회원 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
      List<MemberConsent> memberConsents = List.of(
          createMemberConsent(ConsentType.MARKETING, member),
          createMemberConsent(ConsentType.LOCATION_SERVICE, member)
      );
      doReturn(1L).when(member).getId();
      doReturn(memberConsents).when(memberRepository).findAllMemberConsent(member);

      // when: memberId가 null이므로 내 프로필 조회
      MemberResponse.ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, null);

      // then: 프로필 조회 확인, 이용약관 조회 확인
      assertThat(profileDto).satisfies(dto -> {
        assertThat(dto.getConsentItems()).hasSize(2);
        assertThat(dto.getEmail()).isEqualTo(member.getEmail());
        assertThat(dto.getProvider()).isEqualTo(member.getProvider().name());
        assertThat(dto.getProfileImage().getThumbnailUrl()).isEqualTo(member.getProfileImageFile().getThumbnailUrl());
        assertThat(dto.getNickname()).isEqualTo(member.getNickname());
        assertThat(dto.getDescription()).isEqualTo(member.getDescription());
        assertThat(dto.getTravelType()).isEqualTo(member.getTravelType().getTypeNameWithLocale(Language.KOREAN));
        assertThat(dto.getHashtags()).hasSize(3);
      });

      verify(memberRepository).findAllMemberConsent(member);
    }

    @Test
    @DisplayName("성공 - memberId가 회원의 Id와 동일한 경우 내 프로필 조회")
    void getMemberProfileSuccess_memberIdEquals() {
      // given: 회원 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
      List<MemberConsent> memberConsents = List.of(
          createMemberConsent(ConsentType.MARKETING, member),
          createMemberConsent(ConsentType.LOCATION_SERVICE, member)
      );
      doReturn(1L).when(member).getId();
      doReturn(memberConsents).when(memberRepository).findAllMemberConsent(member);

      // when: memberId가 동일하므로 내 프로필 조회
      MemberResponse.ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, 1L);

      // then: 프로필 조회 확인, 이용약관 조회 확인
      assertThat(profileDto).satisfies(dto -> {
        assertThat(dto.getConsentItems()).hasSize(2);
        assertThat(dto.getEmail()).isEqualTo(member.getEmail());
        assertThat(dto.getProvider()).isEqualTo(member.getProvider().name());
        assertThat(dto.getProfileImage().getThumbnailUrl()).isEqualTo(member.getProfileImageFile().getThumbnailUrl());
        assertThat(dto.getNickname()).isEqualTo(member.getNickname());
        assertThat(dto.getDescription()).isEqualTo(member.getDescription());
        assertThat(dto.getTravelType()).isEqualTo(member.getTravelType().getTypeNameWithLocale(Language.KOREAN));
        assertThat(dto.getHashtags()).hasSize(3);
      });

      verify(memberRepository).findAllMemberConsent(member);
    }

    @Test
    @DisplayName("성공 - memberId가 회원의 Id와 다른 경우 타인 프로필 조회")
    void getMemberProfileSuccess_memberIdNotEquals() {
      // given: 현재 회원, 조회할 회원 설정
      Language language = Language.KOREAN;
      Member member = createMember(language, "nickname");
      Language language2 = Language.ENGLISH;
      Member member2 = createMember(language2, "nickname2");
      MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);

      doReturn(1L).when(member).getId();
      doReturn(Optional.of(member2)).when(memberRepository).findById(2L);

      // when: 현재 회원과 memberId가 다르므로 타인 프로필 조회
      MemberResponse.ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, 2L);

      // then: 프로필 조회 확인, 이용약관 조회하지 않음 확인
      assertThat(profileDto).satisfies(dto -> {
        assertThat(dto.getConsentItems()).isEmpty();
        assertThat(dto.getEmail()).isEqualTo(member2.getEmail());
        assertThat(dto.getProvider()).isEqualTo(member2.getProvider().name());
        assertThat(dto.getProfileImage().getThumbnailUrl()).isEqualTo(member2.getProfileImageFile().getThumbnailUrl());
        assertThat(dto.getNickname()).isEqualTo(member2.getNickname());
        assertThat(dto.getDescription()).isEqualTo(member2.getDescription());
        assertThat(dto.getTravelType()).isEqualTo(member2.getTravelType().getTypeNameWithLocale(Language.KOREAN));
        assertThat(dto.getHashtags()).hasSize(3);
      });

      verify(memberRepository, never()).findAllMemberConsent(any(Member.class));
    }
  }

  @Test
  @DisplayName("언어 설정 변경 TEST")
  void updateLanguage() {
    // given: 언어 변경 요청 DTO 설정
    Language language = Language.KOREAN;
    Member member = createMember(language, "nickname");
    MemberInfoDto memberInfoDto = createMemberInfoDto(language, member);
    Language language2 = Language.ENGLISH;
    MemberRequest.LanguageUpdateDto languageUpdateDto = new MemberRequest.LanguageUpdateDto();
    languageUpdateDto.setLocale(Language.ENGLISH.name());

    // when: 언어 설정 변경
    memberProfileService.updateLanguage(memberInfoDto, languageUpdateDto);

    // then: 변경된 언어 확인
    assertThat(member.getLanguage()).isEqualTo(language2);
  }
}