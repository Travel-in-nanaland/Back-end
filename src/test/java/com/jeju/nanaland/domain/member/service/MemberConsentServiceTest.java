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
import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentItem;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentUpdateDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    return Language.builder()
        .locale(Locale.KOREAN)
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

  private ConsentItem createConsentItem(ConsentType consentType, boolean consent) {
    ConsentItem consentItem = new ConsentItem();
    consentItem.setConsentType(consentType.name());
    consentItem.setConsent(consent);
    return consentItem;
  }

  private MemberConsent createMemberConsent(ConsentType consentType) {
    return MemberConsent.builder()
        .consentType(consentType)
        .consent(true)
        .member(member)
        .build();
  }

  private MemberInfoDto createMemberInfoDto() {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  private ConsentUpdateDto createConsentUpdateDto(ConsentType consentType) {
    ConsentUpdateDto consentUpdateDto = new ConsentUpdateDto();
    consentUpdateDto.setConsentType(consentType.name());
    consentUpdateDto.setConsent(false);
    return consentUpdateDto;
  }

  @Test
  @DisplayName("이용약관 저장 실패 - TERMS_OF_USE가 true가 아닌 경우")
  void createMemberConsentsFail() {
    // when
    List<ConsentItem> consentItems = List.of(
        createConsentItem(ConsentType.TERMS_OF_USE, false),
        createConsentItem(ConsentType.MARKETING, true),
        createConsentItem(ConsentType.LOCATION_SERVICE, false)
    );
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> memberConsentService.createMemberConsents(member, consentItems));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(
        ErrorCode.MEMBER_CONSENT_BAD_REQUEST.getMessage());
  }

  @Test
  @DisplayName("이용약관 저장 성공")
  void createMemberConsentsSuccess() {
    // when
    List<ConsentItem> consentItems = List.of(
        createConsentItem(ConsentType.TERMS_OF_USE, true),
        createConsentItem(ConsentType.MARKETING, true),
        createConsentItem(ConsentType.LOCATION_SERVICE, true)
    );
    memberConsentService.createMemberConsents(member, consentItems);

    // then
    verify(memberConsentRepository, times(1)).saveAll(argumentCaptor.capture());

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

  @Test
  @DisplayName("만료된 이용약관 확인 및 업데이트")
  void checkTermsValidity() {
    // given
    List<MemberConsent> memberConsents = List.of(
        createMemberConsent(ConsentType.TERMS_OF_USE),
        createMemberConsent(ConsentType.MARKETING)
    );

    doReturn(memberConsents).when(memberRepository).findExpiredMemberConsent();

    // when
    memberConsentService.checkTermsValidity();

    // then
    verify(memberRepository, times(1)).findExpiredMemberConsent();
    assertThat(memberConsents)
        .allSatisfy(memberConsent -> assertThat(memberConsent.getConsent()).isFalse());
  }

  @ParameterizedTest
  @DisplayName("이용약관 업데이트 실패 - memberConsent를 찾을 수 없는 경우")
  @EnumSource(value = ConsentType.class, names = "TERMS_OF_USE", mode = Mode.EXCLUDE)
  void updateMemberConsentFail(ConsentType consentType) {
    // given
    ConsentUpdateDto consentUpdateDto = createConsentUpdateDto(consentType);
    doReturn(Optional.empty()).when(memberConsentRepository)
        .findByConsentTypeAndMember(consentType, member);

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> memberConsentService.updateMemberConsent(memberInfoDto, consentUpdateDto));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(
        ErrorCode.MEMBER_CONSENT_NOT_FOUND.getMessage());
  }

  @ParameterizedTest
  @DisplayName("이용약관 업데이트")
  @EnumSource(value = ConsentType.class, names = "TERMS_OF_USE", mode = Mode.EXCLUDE)
  void updateMemberConsentSuccess(ConsentType consentType) {
    // given
    MemberConsent memberConsent = createMemberConsent(consentType);
    ConsentUpdateDto consentUpdateDto = createConsentUpdateDto(consentType);
    doReturn(Optional.of(memberConsent)).when(memberConsentRepository)
        .findByConsentTypeAndMember(consentType, member);

    // when
    memberConsentService.updateMemberConsent(memberInfoDto, consentUpdateDto);

    // then
    verify(memberConsentRepository, times(1)).findByConsentTypeAndMember(any(), any());
    assertThat(memberConsent.getConsent()).isFalse();
  }
}