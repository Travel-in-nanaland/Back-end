package com.jeju.nanaland.domain.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EntityBuilderTest {

  @Autowired
  MemberRepository memberRepository;

  @PersistenceContext
  EntityManager em;

  Language language;

  ImageFile imageFile, imageFile2;

  @BeforeEach
  void init() {
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language);

    imageFile = ImageFile.builder()
        .originUrl("originUrl")
        .thumbnailUrl("thumbnailUrl")
        .build();
    em.persist(imageFile);

    imageFile2 = ImageFile.builder()
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
        .build();
    em.persist(imageFile2);
  }

  @Test
  void NanaBuilderTest() {
    Nana nana1 = Nana.builder()
        .version("ver.1")
        .build();

    em.persist(nana1);

    NanaTitle nanaTitle1 = NanaTitle.builder()
        .nana(nana1)
        .language(language)
        .imageFile(imageFile)
        .notice("notice!")
        .build();
  }

//  @Test
//  void stayBuilderTest() {
//    Stay stay1 = Stay.builder()
//        .imageFile(imageFile)
//        .price(12345)
//        .contact("0101231242")
//        .homepage("httpsL//egegwgeg")
//        .parking("allow?")
//        .ratingAvg(4.15f)
//        .build();
//    em.persist(stay1);
//
//    StayTrans stayTrans1 = StayTrans.builder()
//        .stay(stay1)
//        .language(language)
//        .title("title")
//        .intro("intro")
//        .address("address")
//        .time("10:00 ~ 12:00")
//        .build();
//    em.persist(stayTrans1);
//  }

  @Test
  void marketBuilderTest() {
    Market market1 = Market.builder()
        .imageFile(imageFile)
        .contact("01023244124")
        .homepage("homepageUrl")
        .build();
    em.persist(market1);

    MarketTrans marketTrans1 = MarketTrans.builder()
        .market(market1)
        .language(language)
        .title("title")
        .content("content")
        .address("address")
        .time("time")
        .amenity("amenity")
        .build();
    em.persist(marketTrans1);
  }

  @Test
  void festivalBuilderTest() {
    Festival festival1 = Festival.builder()
        .imageFile(imageFile)
        .contact("contact")
        .homepage("homepage")
        .build();
    em.persist(festival1);

    FestivalTrans festivalTrans1 = FestivalTrans.builder()
        .festival(festival1)
        .language(language)
        .title("title")
        .content("content")
        .address("address")
        .time("time")
        .fee("fee")
        .build();
    em.persist(festivalTrans1);
  }

  @Test
  void natureBuilderTest() {
    Nature nature1 = Nature.builder()
        .imageFile(imageFile)
        .contact("contact")
        .build();
    em.persist(nature1);

    NatureTrans natureTrans1 = NatureTrans.builder()
        .nature(nature1)
        .language(language)
        .title("title")
        .content("content")
        .address("address")
        .intro("intro")
        .details("details")
        .time("time")
        .amenity("amenity")
        .build();
    em.persist(natureTrans1);
  }

  @Test
  void experienceBuilderTest() {
    Experience experience1 = Experience.builder()
        .imageFile(imageFile)
        .contact("contact")
        .type("type")
        .ratingAvg(4.24f)
        .build();
    em.persist(experience1);

    ExperienceTrans experienceTrans1 = ExperienceTrans.builder()
        .experience(experience1)
        .language(language)
        .title("title")
        .content("content")
        .address("address")
        .intro("intro")
        .details("details")
        .time("time")
        .amenity("amenity")
        .build();
    em.persist(experienceTrans1);
  }

  @Test
  void memberStatusRestrictionTest() {
    // status ACTIVE, INACTIVE인 Memeber 생성
    Member memberStatusActive = Member.builder()
        .language(language)
        .email("email")
        .nickname("nickname")
        .provider(Provider.APPLE)
        .providerId(123L)
        .profileImageFile(imageFile)
        .build();

    Member memberStatusInActive = Member.builder()
        .language(language)
        .email("email2")
        .nickname("nickname2")
        .provider(Provider.APPLE)
        .providerId(1234L)
        .profileImageFile(imageFile2)
        .build();
    memberStatusInActive.updateStatus(Status.INACTIVE);
    em.persist(memberStatusActive);
    em.persist(memberStatusInActive);

    // test 1 -> Member field에 private Status status = Status.ACTIVE 테스트
    Assertions.assertThat(memberStatusActive.getStatus()).isEqualTo(Status.ACTIVE);
    Assertions.assertThat(memberStatusActive.getLevel()).isEqualTo(1);
    Assertions.assertThat(memberStatusInActive.getStatus()).isEqualTo(Status.INACTIVE);
    Assertions.assertThat(memberStatusInActive.getLevel()).isEqualTo(1);

    // test 2 -> JpaMethod 테스트
    Optional<Member> member1 = memberRepository.findMemberById(memberStatusActive.getId());
    Optional<Member> member2 = memberRepository.findMemberById(memberStatusInActive.getId());
    Optional<Member> member3 = memberRepository.findByProviderAndProviderId(Provider.APPLE, 1234L);

    Assertions.assertThat(member1).isNotNull();
    Assertions.assertThat(member2.isPresent()).isFalse();
    Assertions.assertThat(member3.isPresent()).isFalse();

    // test 3 -> queryDsl 테스트
    MemberInfoDto memberInfoDto = memberRepository.findMemberWithLanguage(
        memberStatusInActive.getId());
    Assertions.assertThat(memberInfoDto).isNull();
  }
}
