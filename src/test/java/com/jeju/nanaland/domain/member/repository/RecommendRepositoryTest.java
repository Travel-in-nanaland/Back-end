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
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RecommendRepositoryTest {

  @Autowired
  RecommendRepository recommendRepository;

  @Autowired
  EntityManager em;

  Map<Category, TriFunction<Language, TravelType, String, Recommend>> initFuncMap;

  Map<Category, TriFunction<Long, Language, TravelType, RecommendPostDto>> findRecommendPostDtoMap;

  Map<Category, String[]> titleAndIntroductionMap;

  @PostConstruct
  void initMap() {
    initFuncMap = new HashMap<>();
    initFuncMap.put(Category.NATURE, this::initNatureRecommend);
    initFuncMap.put(Category.MARKET, this::initMarketRecommend);
    initFuncMap.put(Category.FESTIVAL, this::initFestivalRecommend);
    initFuncMap.put(Category.EXPERIENCE, this::initExperienceRecommend);
    initFuncMap.put(Category.NANA, this::initNanaRecommend);

    findRecommendPostDtoMap = new HashMap<>();
    findRecommendPostDtoMap.put(Category.NATURE, recommendRepository::findNatureRecommendPostDto);
    findRecommendPostDtoMap.put(Category.MARKET, recommendRepository::findMarketRecommendPostDto);
    findRecommendPostDtoMap.put(Category.FESTIVAL,
        recommendRepository::findFestivalRecommendPostDto);
    findRecommendPostDtoMap.put(Category.EXPERIENCE,
        recommendRepository::findExperienceRecommendPostDto);
    findRecommendPostDtoMap.put(Category.NANA, recommendRepository::findNanaRecommendPostDto);

    titleAndIntroductionMap = new HashMap<>();
    titleAndIntroductionMap.put(Category.NATURE, new String[]{"7대자연 제목", "7대자연 설명"});
    titleAndIntroductionMap.put(Category.MARKET, new String[]{"전통시장 제목", "전통시장 설명"});
    titleAndIntroductionMap.put(Category.FESTIVAL, new String[]{"축제 제목", "축제 설명"});
    titleAndIntroductionMap.put(Category.EXPERIENCE, new String[]{"이색체험 제목", "이색체험 설명"});
    titleAndIntroductionMap.put(Category.NANA, new String[]{"이색체험 제목", "이색체험 설명"});

  }

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
    initNatureRecommend(language, travelType, null);
    initExperienceRecommend(language, travelType, null);
    initNanaRecommend(language, travelType, null);

    // when
    List<Recommend> result = recommendRepository.findAllByTravelType(travelType);

    // then
    Assertions.assertThat(result.size()).isEqualTo(3);
  }


  @ParameterizedTest
  @EnumSource(value = Category.class, names = {"RESTAURANT", "NANA_CONTENT"}, mode = Mode.EXCLUDE)
  @DisplayName("추천 게시물 별도 제목이 없을 때 조회")
  void findRecommendPostDtoTest(Category category) {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    Recommend recommend = initFuncMap.get(category).apply(language, travelType, null);

    // when
    RecommendPostDto recommendPostDto = findRecommendPostDtoMap.get(category).apply(
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto.getTitle())
        .isEqualTo(titleAndIntroductionMap.get(category)[0]); // title
    Assertions.assertThat(recommendPostDto.getIntroduction())
        .isEqualTo(titleAndIntroductionMap.get(category)[1]); // introduction
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = {"RESTAURANT", "NANA_CONTENT"}, mode = Mode.EXCLUDE)
  @DisplayName("추천 게시물 별도 제목이 있을 때")
  void findRecommendPostDtoWithRecommendTitleTest(Category category) {
    // given
    Language language = initLanguageKorean();
    TravelType travelType = TravelType.GAMGYUL_ICECREAM;
    String randomTitle = UUID.randomUUID().toString();
    Recommend recommend = initFuncMap.get(category).apply(language, travelType, randomTitle);

    // when
    RecommendPostDto recommendPostDto = findRecommendPostDtoMap.get(category).apply(
        recommend.getPost().getId(), Language.KOREAN, TravelType.GAMGYUL_ICECREAM);

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getTitle)
        .isEqualTo(randomTitle);
  }

  Language initLanguageKorean() {
    Language language = Language.KOREAN;
    return language;
  }

  Recommend initNatureRecommend(Language language, TravelType travelType, String recommendTitle) {
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
        .title(titleAndIntroductionMap.get(category)[0])
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
        .title(recommendTitle)
        .introduction(titleAndIntroductionMap.get(category)[1])
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initMarketRecommend(Language language, TravelType travelType, String recommendTitle) {
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
        .title(titleAndIntroductionMap.get(category)[0])
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
        .title(recommendTitle)
        .introduction(titleAndIntroductionMap.get(category)[1])
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initFestivalRecommend(Language language, TravelType travelType, String recommendTitle) {
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
        .title(titleAndIntroductionMap.get(category)[0])
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
        .title(recommendTitle)
        .introduction(titleAndIntroductionMap.get(category)[1])
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initExperienceRecommend(Language language, TravelType travelType,
      String recommendTitle) {
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
        .title(titleAndIntroductionMap.get(category)[0])
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
        .title(recommendTitle)
        .introduction(titleAndIntroductionMap.get(category)[1])
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }

  Recommend initNanaRecommend(Language language, TravelType travelType, String recommendTitle) {
    Category category = Category.NANA;

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Nana nana = Nana.builder()
        .version("1")
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
    em.persist(nana);
    NanaTitle nanaTitle = NanaTitle.builder()
        .heading(titleAndIntroductionMap.get(category)[0])
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
        .title(recommendTitle)
        .introduction(titleAndIntroductionMap.get(category)[1])
        .language(language)
        .build();
    em.persist(recommendTrans);

    return recommend;
  }
}
