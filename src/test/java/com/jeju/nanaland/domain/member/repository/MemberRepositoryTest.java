package com.jeju.nanaland.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
class MemberRepositoryTest {

  @Autowired
  private JPAQueryFactory queryFactory;

  @PersistenceContext
  private EntityManager entityManager;

  @InjectMocks
  private MemberRepositoryImpl memberRepository;

  private MemberTravelType memberTravelType;
  private ImageFile imageFile;

  @BeforeEach
  public void setUp() {
    memberRepository = new MemberRepositoryImpl(queryFactory);
    memberTravelType = createMemberTravelType();
    imageFile = createImageFile();
  }

  private Language createLanguage(Locale locale) {
    Language language = Language.builder()
        .locale(locale)
        .dateFormat("yy-MM-dd")
        .build();
    entityManager.persist(language);
    return language;
  }

  private MemberTravelType createMemberTravelType() {
    memberTravelType = MemberTravelType.builder()
        .travelType(TravelType.NONE)
        .build();
    entityManager.persist(memberTravelType);
    return memberTravelType;
  }

  private ImageFile createImageFile() {
    imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    entityManager.persist(imageFile);
    return imageFile;
  }

  private Member createMember(Language language) {
    Member member = Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .memberTravelType(memberTravelType)
        .build();
    entityManager.persist(member);
    return member;
  }

  private MemberConsent createMemberConsent(ConsentType consentType, boolean consent,
      LocalDateTime consentDate, Member member) {
    MemberConsent memberConsent = MemberConsent.builder()
        .consentType(consentType)
        .consent(consent)
        .consentDate(consentDate)
        .member(member)
        .build();
    entityManager.persist(memberConsent);
    return memberConsent;
  }

  @Test
  @DisplayName("memberId로 language를 join하여 member 조회")
  void findMemberWithLanguage() {
    // given
    Language language = createLanguage(Locale.KOREAN);
    Member member = createMember(language);

    // when
    MemberInfoDto memberWithLanguage = memberRepository.findMemberWithLanguage(member.getId());

    // then
    assertThat(memberWithLanguage).isNotNull();
    assertThat(memberWithLanguage.getMember().getId()).isEqualTo(member.getId());
    assertThat(memberWithLanguage.getMember().getLanguage()).isEqualTo(language);
    assertThat(memberWithLanguage.getMember().getStatus()).isEqualTo(Status.ACTIVE);
  }

  @Test
  @DisplayName("1년 6개월이 지나 만료된 이용약관 조회")
  void findExpiredMemberConsent() {
    // given
    Language language = createLanguage(Locale.KOREAN);
    Member member = createMember(language);
    LocalDateTime pastDate = LocalDate.now().minusYears(1).minusMonths(7).atStartOfDay();

    createMemberConsent(ConsentType.TERMS_OF_USE, true, pastDate, member);
    createMemberConsent(ConsentType.MARKETING, true, null, member);
    createMemberConsent(ConsentType.LOCATION_SERVICE, false, pastDate, member);

    // when
    List<MemberConsent> expiredMemberConsent = memberRepository.findExpiredMemberConsent();

    // then
    assertThat(expiredMemberConsent).hasSize(1);
    assertThat(expiredMemberConsent.get(0).getConsentType()).isEqualTo(ConsentType.TERMS_OF_USE);
  }

  @Test
  void findInactiveMembersForWithdrawalDate() {
  }

  @Test
  void findMemberConsentByMember() {
  }
}