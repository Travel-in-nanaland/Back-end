package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
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
  private MemberTravelType memberTravelType;
  private Member member;
  private MemberInfoDto memberInfoDto;
  @Captor
  private ArgumentCaptor<List<MemberConsent>> argumentCaptor;

  @BeforeEach
  void setUp() {
    language = createLanguage();
    imageFile = createImageFile();
    memberTravelType = createMemberTravelType();
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

  private MemberTravelType createMemberTravelType() {
    return MemberTravelType.builder()
        .travelType(TravelType.NONE)
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
        .memberTravelType(memberTravelType)
        .build());
  }

  private ConsentItem createConsentItem(ConsentType consentType) {
    ConsentItem consentItem = new ConsentItem();
    consentItem.setConsentType(consentType.name());
    consentItem.setConsent(true);
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
  @DisplayName("이용약관 저장")
  void createMemberConsentsSuccess() {
    // when
    List<ConsentItem> consentItems = List.of(
        createConsentItem(ConsentType.TERMS_OF_USE),
        createConsentItem(ConsentType.MARKETING),
        createConsentItem(ConsentType.LOCATION_SERVICE)
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
  @DisplayName("이용약관 업데이트")
  @EnumSource(value = ConsentType.class, names = "TERMS_OF_USE", mode = Mode.EXCLUDE)
  void updateMemberConsent(ConsentType consentType) {
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