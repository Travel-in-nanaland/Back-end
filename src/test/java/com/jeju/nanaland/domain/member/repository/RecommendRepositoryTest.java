package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.RecommendTrans;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
public class RecommendRepositoryTest {

  @Autowired
  RecommendRepository recommendRepository;

  @Autowired
  EntityManager em;

  @DisplayName("타입을 통해 추천 게시물 조회")
  @Test
  void findAllByMemberTravelTypeTest() {
    /**
     * given
     *
     * 한국어, 감귤 아이스크림 타입 Recommend에 nature, experience, nana 추가
     */
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    initNatureRecommend(language, travelType);
    initExperienceRecommend(language, travelType);
    initNanaRecommend(language, travelType);

    // when
    List<Recommend> result = recommendRepository.findAllByTravelType(travelType);

    // then
    Assertions.assertThat(result.size()).isEqualTo(3);
  }

  @DisplayName("7대자연 추천 게시물 조회")
  @Test
  void findNatureRecommendPostDtoTest() {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initNatureRecommend(language, travelType);

    // when
    RecommendPostDto recommendPostDto = recommendRepository.findNatureRecommendPostDto(
        recommend.getPost().getId(), Locale.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("7대자연 설명");
  }

  @DisplayName("전통시장 추천 게시물 조회")
  @Test
  void findMarketRecommendPostDtoTest() {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initMarketRecommend(language, travelType);

    // when
    RecommendPostDto recommendPostDto = recommendRepository.findMarketRecommendPostDto(
        recommend.getPost().getId(), Locale.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("전통시장 설명");
  }

  @DisplayName("축제 추천 게시물 조회")
  @Test
  void findFestivalRecommendPostDtoTest() {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initFestivalRecommend(language, travelType);

    // when
    RecommendPostDto recommendPostDto = recommendRepository.findFestivalRecommendPostDto(
        recommend.getPost().getId(), Locale.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("축제 설명");
  }

  @DisplayName("이색체험 추천 게시물 조회")
  @Test
  void findExperienceRecommendPostDtoTest() {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initExperienceRecommend(language, travelType);

    // when
    RecommendPostDto recommendPostDto = recommendRepository.findExperienceRecommendPostDto(
        recommend.getPost().getId(), Locale.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("이색체험 설명");
  }

  @DisplayName("나나스픽 추천 게시물 조회")
  @Test
  void findNanaRecommendPostDtoTest() {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initNanaRecommend(language, travelType);

    // when
    RecommendPostDto recommendPostDto = recommendRepository.findNanaRecommendPostDto(
        recommend.getPost().getId(), Locale.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("나나스픽 설명");
  }

  Language initLanguageKorean() {
    Language language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    return language;
  }

  Recommend initNatureRecommend(Language language, TravelType travelType) {
    Category category = Category.NATURE;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Nature nature = Nature.builder()
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(nature);
    NatureTrans natureTrans = NatureTrans.builder()
        .title("7대자연 제목")
        .nature(nature)
        .language(language)
        .build();
    em.persist(natureTrans);

    Recommend recommend = Recommend.builder()
        .travelType(travelType)
        .post(nature)
        .category(category)
        .firstImageFile(imageFile)
        .build();
    em.persist(recommend);
    RecommendTrans recommendTrans = RecommendTrans.builder()
        .recommend(recommend)
        .introduction("7대자연 설명")
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initMarketRecommend(Language language, TravelType travelType) {
    Category category = Category.MARKET;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Market market = Market.builder()
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(market);
    MarketTrans marketTrans = MarketTrans.builder()
        .title("전통시장 제목")
        .market(market)
        .language(language)
        .build();
    em.persist(marketTrans);

    Recommend recommend = Recommend.builder()
        .travelType(travelType)
        .post(market)
        .category(category)
        .firstImageFile(imageFile)
        .build();
    em.persist(recommend);
    RecommendTrans recommendTrans = RecommendTrans.builder()
        .recommend(recommend)
        .introduction("전통시장 설명")
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initFestivalRecommend(Language language, TravelType travelType) {
    Category category = Category.FESTIVAL;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Festival festival = Festival.builder()
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(festival);
    FestivalTrans festivalTrans = FestivalTrans.builder()
        .title("축제 제목")
        .festival(festival)
        .language(language)
        .build();
    em.persist(festivalTrans);

    Recommend recommend = Recommend.builder()
        .travelType(travelType)
        .post(festival)
        .category(category)
        .firstImageFile(imageFile)
        .build();
    em.persist(recommend);
    RecommendTrans recommendTrans = RecommendTrans.builder()
        .recommend(recommend)
        .introduction("축제 설명")
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initExperienceRecommend(Language language, TravelType travelType) {
    Category category = Category.EXPERIENCE;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Experience experience = Experience.builder()
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(experience);
    ExperienceTrans experienceTrans = ExperienceTrans.builder()
        .title("이색체험 제목")
        .experience(experience)
        .language(language)
        .build();
    em.persist(experienceTrans);

    Recommend recommend = Recommend.builder()
        .travelType(travelType)
        .post(experience)
        .category(category)
        .firstImageFile(imageFile)
        .build();
    em.persist(recommend);
    RecommendTrans recommendTrans = RecommendTrans.builder()
        .recommend(recommend)
        .introduction("이색체험 설명")
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initNanaRecommend(Language language, TravelType travelType) {
    Category category = Category.NANA;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Nana nana = Nana.builder()
        .version("1")
        .firstImageFile(imageFile)
        .nanaTitleImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(nana);
    NanaTitle nanaTitle = NanaTitle.builder()
        .heading("나나스픽 제목")
        .nana(nana)
        .language(language)
        .build();
    em.persist(nanaTitle);

    Recommend recommend = Recommend.builder()
        .travelType(travelType)
        .post(nana)
        .category(category)
        .firstImageFile(imageFile)
        .build();
    em.persist(recommend);
    RecommendTrans recommendTrans = RecommendTrans.builder()
        .recommend(recommend)
        .introduction("나나스픽 설명")
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }
}
