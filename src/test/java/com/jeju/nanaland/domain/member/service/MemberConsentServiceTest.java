package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class MemberConsentServiceTest {

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private MemberConsentRepository memberConsentRepository;
  @InjectMocks
  private MemberConsentService memberConsentService;

  private Language language;
  private ImageFile imageFile;
  private Member member;
  private MemberInfoDto memberInfoDto;
  @Captor
  private ArgumentCaptor<List<MemberConsent>> argumentCaptor;

  @BeforeEach
  void setUp() {
    language = createLanguage();
    imageFile = createImageFile();
    member = createMember();
    memberInfoDto = createMemberInfoDto();
  }

  private Language createLanguage() {
    return Language.KOREAN;
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
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("MALE")
        .birthDate(LocalDate.now())
        .provider(Provider.valueOf("GOOGLE"))
        .providerId("123")
        .travelType(TravelType.NONE)
        .build());
  }

  private MemberRequest.ConsentItem createConsentItem(ConsentType consentType, boolean consent) {
    MemberRequest.ConsentItem consentItem = new MemberRequest.ConsentItem();
    consentItem.setConsentType(consentType.name());
    consentItem.setConsent(consent);
    return consentItem;
  }

  private MemberConsent createMemberConsent(ConsentType consentType, boolean consent) {
    return MemberConsent.builder()
        .consentType(consentType)
        .consent(consent)
        .member(member)
        .build();
  }

  private MemberInfoDto createMemberInfoDto() {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  private MemberRequest.ConsentUpdateDto createConsentUpdateDto(ConsentType consentType,
      boolean consent) {
    MemberRequest.ConsentUpdateDto consentUpdateDto = new MemberRequest.ConsentUpdateDto();
    consentUpdateDto.setConsentType(consentType.name());
    consentUpdateDto.setConsent(consent);
    return consentUpdateDto;
  }

  @Nested
  @DisplayName("이용약관 저장 TEST")
  class CreateMemberConsents {
    @Test
    @DisplayName("실패 - TERMS_OF_USE가 true가 아닌 경우")
    void createMemberConsentsFail_termsOfUseFalse() {

      // given: TERMS_OF_USE를 false로 설정
      List<MemberRequest.ConsentItem> consentItems = List.of(
          createConsentItem(ConsentType.TERMS_OF_USE, false),
          createConsentItem(ConsentType.MARKETING, true),
          createConsentItem(ConsentType.LOCATION_SERVICE, false)
      );

      // when: 이용약관 생성
      BadRequestException badRequestException = assertThrows(BadRequestException.class,
          () -> memberConsentService.createMemberConsents(member, consentItems));

      // then: ErrorCode 검증
      assertThat(badRequestException.getMessage()).isEqualTo(
          ErrorCode.MEMBER_CONSENT_BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("실패 - TERMS_OF_USE가 제공되지 않은 경우")
    void createMemberConsentsFail_termsOfUseNotProvided() {

      // given: TERMS_OF_USE를 포함하지 않도록 설정
      List<MemberRequest.ConsentItem> consentItems = List.of(
          createConsentItem(ConsentType.MARKETING, true),
          createConsentItem(ConsentType.LOCATION_SERVICE, false)
      );

      // when: 이용약관 생성
      BadRequestException badRequestException = assertThrows(BadRequestException.class,
          () -> memberConsentService.createMemberConsents(member, consentItems));

      // then: ErrorCode 검증
      assertThat(badRequestException.getMessage()).isEqualTo(
          ErrorCode.MEMBER_CONSENT_BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("성공")
    void createMemberConsentsSuccess() {

      // given: 각 동의 항목을 true로 설정
      List<MemberRequest.ConsentItem> consentItems = List.of(
          createConsentItem(ConsentType.TERMS_OF_USE, true),
          createConsentItem(ConsentType.MARKETING, true),
          createConsentItem(ConsentType.LOCATION_SERVICE, true)
      );

      // when: 이용약관 생성
      memberConsentService.createMemberConsents(member, consentItems);

      // then: 캡쳐된 이용약관 리스트의 크기 검증 및 타입 검증
      verify(memberConsentRepository).saveAll(argumentCaptor.capture());

      List<MemberConsent> capturedMemberConsents = argumentCaptor.getValue();
      assertThat(capturedMemberConsents)
          .hasSize(3)
          .allSatisfy(memberConsent -> {
            assertThat(memberConsent.getMember()).isEqualTo(member);
            assertThat(memberConsent.getConsent()).isTrue();
            assertThat(memberConsent.getConsentDate()).isNotNull();
          });
      assertThat(capturedMemberConsents.get(0).getConsentType()).isEqualTo(ConsentType.TERMS_OF_USE);
      assertThat(capturedMemberConsents.get(1).getConsentType()).isEqualTo(ConsentType.MARKETING);
      assertThat(capturedMemberConsents.get(2).getConsentType()).isEqualTo(
          ConsentType.LOCATION_SERVICE);
    }
  }

  @Test
  @DisplayName("만료된 이용약관 확인 및 업데이트 성공 TEST")
  void checkTermsValidity() {

    // given: 이용약관 설정
    List<MemberConsent> memberConsents = List.of(
        createMemberConsent(ConsentType.TERMS_OF_USE, true),
        createMemberConsent(ConsentType.MARKETING, true)
    );

    doReturn(memberConsents).when(memberRepository).findAllExpiredMemberConsent();

    // when: 만료된 이용약관 확인 및 업데이트
    memberConsentService.checkTermsValidity();

    // then: 각 동의 항목이 false인지 검증
    assertThat(memberConsents)
        .allSatisfy(memberConsent -> assertThat(memberConsent.getConsent()).isFalse());
  }

  @Nested
  @DisplayName("이용약관 업데이트 TEST")
  class UpdateMemberConsent {

    @ParameterizedTest
    @DisplayName("실패 - memberConsent를 찾을 수 없는 경우")
    @EnumSource(value = ConsentType.class, names = "TERMS_OF_USE", mode = Mode.EXCLUDE)
    void updateMemberConsentFail_memberConsentNotFound(ConsentType consentType) {

      // given: 이용약관 수정 DTO 설정 및 memberConsent를 찾을 수 없도록 설정
      MemberRequest.ConsentUpdateDto consentUpdateDto = createConsentUpdateDto(consentType, false);
      doReturn(Optional.empty()).when(memberConsentRepository)
          .findByConsentTypeAndMember(consentType, member);

      // when: 이용약관 동의 여부 수정
      NotFoundException notFoundException = assertThrows(NotFoundException.class,
          () -> memberConsentService.updateMemberConsent(memberInfoDto, consentUpdateDto));

      // then: ErrorCode 검증
      assertThat(notFoundException.getMessage()).isEqualTo(
          ErrorCode.MEMBER_CONSENT_NOT_FOUND.getMessage());
    }

    @ParameterizedTest
    @DisplayName("성공")
    @EnumSource(value = ConsentType.class, names = "TERMS_OF_USE", mode = Mode.EXCLUDE)
    void updateMemberConsentSuccess(ConsentType consentType) {

      // given: 이용약관 수정 DTO 설정 및 memberConsent를 찾을 있도록 설정
      MemberRequest.ConsentUpdateDto consentUpdateDto = createConsentUpdateDto(consentType, true);
      MemberConsent memberConsent = createMemberConsent(consentType, false);
      doReturn(Optional.of(memberConsent)).when(memberConsentRepository)
          .findByConsentTypeAndMember(consentType, member);

      // when: 이용약관 동의 여부 수정
      memberConsentService.updateMemberConsent(memberInfoDto, consentUpdateDto);

      // then: ErrorCode 검증
      assertThat(memberConsent.getConsent()).isTrue();
    }
  }
}