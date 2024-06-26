package com.jeju.nanaland.domain.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    // TODO: nanaTitleImageFile 삭제 후 수정 필요
    Nana nana1 = Nana.builder()
        .version("ver.1")
        .firstImageFile(imageFile)
        .nanaTitleImageFile(imageFile)
        .priority(0L)
        .build();

    em.persist(nana1);

    NanaTitle nanaTitle1 = NanaTitle.builder()
        .nana(nana1)
        .language(language)
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
        .firstImageFile(imageFile)
        .contact("01023244124")
        .homepage("homepageUrl")
        .priority(0L)
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
        .firstImageFile(imageFile)
        .contact("contact")
        .homepage("homepage")
        .priority(0L)
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
        .firstImageFile(imageFile)
        .contact("contact")
        .priority(0L)
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
        .firstImageFile(imageFile)
        .contact("contact")
        .type("type")
        .ratingAvg(4.24f)
        .priority(0L)
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
}
