package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
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
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

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
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

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
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

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
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

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
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("나나스픽 설명");
  }

  Language initLanguageKorean() {
    return Language.KOREAN;
  }

  Recommend initNatureRecommend(Language language, TravelType travelType) {
    Category category = Category.NATURE;

    ImageFile imageFile = TestUtil.findImageFileByNumber(em, 1);

    Nature nature = TestUtil.findNatureList(em, 1).get(0);
    NatureTrans natureTrans = TestUtil.findNatureTransByNature(em, nature);

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

    ImageFile imageFile = TestUtil.findImageFileByNumber(em, 1);

    Market market = TestUtil.findMarketList(em, 1).get(0);
    MarketTrans marketTrans = TestUtil.findMarketTransByMarket(em, market);

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

    ImageFile imageFile = TestUtil.findImageFileByNumber(em, 1);

    Festival festival = TestUtil.findFestivalByStringSeason(em, "겨울");
    FestivalTrans festivalTrans = TestUtil.findFestivalTransByFestival(em, festival);

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

    ImageFile imageFile = TestUtil.findImageFileByNumber(em, 1);

    Experience experience = TestUtil.findExperienceList(em, 1).get(0);
    ExperienceTrans experienceTrans = TestUtil.findExperienceTransByExperience(em, experience);

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

    ImageFile imageFile = TestUtil.findImageFileByNumber(em, 1);

    Nana nana = TestUtil.findNana(em, 1);
    NanaTitle nanaTitle = TestUtil.findNanaTitleByNana(em, nana);

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
