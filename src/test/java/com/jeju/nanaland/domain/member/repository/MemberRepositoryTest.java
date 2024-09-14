package com.jeju.nanaland.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.entity.enums.WithdrawalType;
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
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {

  @Autowired
  private JPAQueryFactory queryFactory;

  @PersistenceContext
  private EntityManager entityManager;

  @InjectMocks
  private MemberRepositoryImpl memberRepository;

  private ImageFile imageFile;
  private Language language;

  @BeforeEach
  public void setUp() {
    memberRepository = new MemberRepositoryImpl(queryFactory);
    imageFile = createImageFile();
    language = createLanguage();
  }

  private Language createLanguage() {
    language = Language.KOREAN;
    return language;
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
        .travelType(TravelType.NONE)
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

  private void createMemberWithdrawal(Member member) {
    MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
        .withdrawalType(WithdrawalType.INCONVENIENT_COMMUNITY)
        .member(member)
        .build();
    memberWithdrawal.updateWithdrawalDate();
    member.updateStatus(Status.INACTIVE);
    entityManager.persist(memberWithdrawal);
  }

  @Test
  @DisplayName("회원 정보 조회 TEST")
  void findMemberInfoDto() {
    // given: 회원 설정
    Member member = createMember(language);

    // when: MemberInfoDto 조회
    MemberInfoDto memberInfoDto = memberRepository.findMemberInfoDto(member.getId());

    // then: MemberInfoDto(member, language) 확인
    assertThat(memberInfoDto).isNotNull();
    assertThat(memberInfoDto.getMember().getId()).isEqualTo(member.getId());
    assertThat(memberInfoDto.getMember().getEmail()).isEqualTo(member.getEmail());
    assertThat(memberInfoDto.getMember().getNickname()).isEqualTo(member.getNickname());
    assertThat(memberInfoDto.getMember().getLanguage()).isEqualTo(language);
    assertThat(memberInfoDto.getMember().getStatus()).isEqualTo(Status.ACTIVE);
  }

  @Test
  @DisplayName("동의일이 1년 6개월이 지나 만료된 이용약관 조회 TEST")
  void findAllExpiredMemberConsent() {
    // given: 회원 설정, 만료된 이용약관 설정
    Member member = createMember(language);
    LocalDateTime pastDate = LocalDate.now().minusYears(1).minusMonths(7).atStartOfDay();

    MemberConsent expiredConsent = createMemberConsent(ConsentType.TERMS_OF_USE, true, pastDate,
        member);
    createMemberConsent(ConsentType.MARKETING, true, null, member);
    createMemberConsent(ConsentType.LOCATION_SERVICE, false, pastDate, member);

    // when: 동의일이 1년 6개월이 지나 만료된 이용약관 조회
    List<MemberConsent> expiredMemberConsents = memberRepository.findAllExpiredMemberConsent();

    // then: 조회된 이용약관 확인
    assertThat(expiredMemberConsents).hasSize(1);
    assertThat(expiredMemberConsents.get(0)).isEqualTo(expiredConsent);
    assertThat(expiredMemberConsents.get(0).getConsentType()).isEqualTo(ConsentType.TERMS_OF_USE);
    assertThat(expiredMemberConsents.get(0).getConsent()).isTrue();
    assertThat(expiredMemberConsents.get(0).getConsentDate()).isEqualTo(pastDate);
  }

  @Test
  @DisplayName("비활성화 후 3개월이 지난 회원 조회 TEST")
  void findAllInactiveMember() {
    // given: 비활성화 및 탈퇴한 회원 설정
    Member member = createMember(language);
    createMemberWithdrawal(member);

    // when: 비활성화 후 3개월이 지난 회원 조회
    List<Member> inactiveMembers = memberRepository.findAllInactiveMember();

    // then: 조회된 회원 확인
    assertThat(inactiveMembers).hasSize(1);
    assertThat(inactiveMembers.get(0)).isEqualTo(member);
    assertThat(inactiveMembers.get(0).getId()).isEqualTo(member.getId());
    assertThat(inactiveMembers.get(0).getStatus()).isEqualTo(Status.INACTIVE);
  }

  @Test
  @DisplayName("회원의 이용약관 조회 TEST")
  void findAllMemberConsent() {
    // given: 회원 설정, 이용약관 설정
    Member member = createMember(language);
    MemberConsent marketingConsent = createMemberConsent(ConsentType.MARKETING, true, null, member);
    MemberConsent locationConsent = createMemberConsent(ConsentType.LOCATION_SERVICE, false, null,
        member);

    // when: 회원의 이용약관 조회
    List<MemberConsent> memberConsents = memberRepository.findAllMemberConsent(member);

    // then: 조회된 이용약관 확인
    assertThat(memberConsents).containsExactlyInAnyOrder(marketingConsent, locationConsent);
    assertThat(memberConsents).extracting("consentType")
        .containsExactlyInAnyOrder(ConsentType.MARKETING, ConsentType.LOCATION_SERVICE);
    assertThat(memberConsents).extracting("consent")
        .containsExactlyInAnyOrder(true, false);
  }
}