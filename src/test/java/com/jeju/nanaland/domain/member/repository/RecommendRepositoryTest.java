package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
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
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
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
import org.junit.jupiter.api.BeforeEach;
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

  MemberTravelType memberTravelType;

  @BeforeEach
  void init() {
    // memberTravelType init
    memberTravelType = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_ICECREAM)
        .build();
    em.persist(memberTravelType);

    // category init
    Category natureCategory = Category.builder()
        .content(CategoryContent.NATURE)
        .build();
    Category experienceCategory = Category.builder()
        .content(CategoryContent.EXPERIENCE)
        .build();
    Category festivalCategory = Category.builder()
        .content(CategoryContent.FESTIVAL)
        .build();
    Category marketCategory = Category.builder()
        .content(CategoryContent.MARKET)
        .build();
    Category nanaCategory = Category.builder()
        .content(CategoryContent.NANA)
        .build();
    em.persist(natureCategory);
    em.persist(experienceCategory);
    em.persist(festivalCategory);
    em.persist(marketCategory);
    em.persist(nanaCategory);

    // language init
    Language korean = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(korean);

    // image file init
    ImageFile imageFile1 = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile1);

    // nature, natureTrans init
    Nature nature1 = Nature.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(nature1);
    NatureTrans natureTrans1 = NatureTrans.builder()
        .title("7대자연 제목")
        .nature(nature1)
        .language(korean)
        .build();
    em.persist(natureTrans1);

    // festival, festivalTrans init
    Festival festival1 = Festival.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(festival1);
    FestivalTrans festivalTrans1 = FestivalTrans.builder()
        .title("축제 제목")
        .language(korean)
        .festival(festival1)
        .build();
    em.persist(festivalTrans1);

    // market, marketTrans init
    Market market1 = Market.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(market1);
    MarketTrans marketTrans1 = MarketTrans.builder()
        .title("전통시장 제목")
        .language(korean)
        .market(market1)
        .build();
    em.persist(marketTrans1);

    // experience, experienceTrans init
    Experience experience1 = Experience.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(experience1);
    ExperienceTrans experienceTrans1 = ExperienceTrans.builder()
        .title("이색체험 제목")
        .experience(experience1)
        .language(korean)
        .build();
    em.persist(experienceTrans1);

    // nana, nanaTitle init
    Nana nana1 = Nana.builder()
        .version("1")
        .nanaTitleImageFile(imageFile1)
        .build();
    em.persist(nana1);
    NanaTitle nanaTitle1 = NanaTitle.builder()
        .heading("나나스픽 제목")
        .nana(nana1)
        .language(korean)
        .build();
    em.persist(nanaTitle1);

    // recommend, recommendTrans init
    Recommend recommend1 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(nature1.getId())
        .category(natureCategory)
        .build();
    em.persist(recommend1);
    RecommendTrans recommendTrans1 = RecommendTrans.builder()
        .recommend(recommend1)
        .introduction("7대자연 설명")
        .language(korean)
        .build();
    em.persist(recommendTrans1);
    Recommend recommend2 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(experience1.getId())
        .category(experienceCategory)
        .build();
    em.persist(recommend2);
    RecommendTrans recommendTrans2 = RecommendTrans.builder()
        .recommend(recommend2)
        .introduction("이색체험 설명")
        .language(korean)
        .build();
    em.persist(recommendTrans2);
    Recommend recommend3 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(festival1.getId())
        .category(festivalCategory)
        .build();
    em.persist(recommend3);
    RecommendTrans recommendTrans3 = RecommendTrans.builder()
        .recommend(recommend3)
        .introduction("축제 설명")
        .language(korean)
        .build();
    em.persist(recommendTrans3);
    Recommend recommend4 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(market1.getId())
        .category(marketCategory)
        .build();
    em.persist(recommend4);
    RecommendTrans recommendTrans4 = RecommendTrans.builder()
        .recommend(recommend4)
        .introduction("전통시장 설명")
        .language(korean)
        .build();
    em.persist(recommendTrans4);
    Recommend recommend5 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(nana1.getId())
        .category(nanaCategory)
        .build();
    em.persist(recommend5);
    RecommendTrans recommendTrans5 = RecommendTrans.builder()
        .recommend(recommend5)
        .introduction("나나 설명")
        .language(korean)
        .build();
    em.persist(recommendTrans5);
  }

  @DisplayName("타입을 통해 추천 게시물 조회")
  @Test
  void findAllByMemberTravelTypeTest() {
    // given
    // when
    List<Recommend> result = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // then
    Assertions.assertThat(result.size()).isEqualTo(5);
  }

  @DisplayName("7대자연 추천 게시물 조회")
  @Test
  void findNatureRecommendPostDtoTest() {
    // given
    List<Recommend> recommendList = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // when
    RecommendPostDto recommendPostDto = null;
    for (Recommend recommend : recommendList) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      if (category.getContent().equals(CategoryContent.NATURE)) {
        recommendPostDto = recommendRepository.findNatureRecommendPostDto(
            postId, Locale.KOREAN,
            memberTravelType.getTravelType());
      }
    }

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("7대자연 설명");
  }

  @DisplayName("축제 추천 게시물 조회")
  @Test
  void findFestivalRecommendPostDtoTest() {
    // given
    List<Recommend> recommendList = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // when
    RecommendPostDto recommendPostDto = null;
    for (Recommend recommend : recommendList) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      if (category.getContent().equals(CategoryContent.FESTIVAL)) {
        recommendPostDto = recommendRepository.findFestivalRecommendPostDto(
            postId, Locale.KOREAN,
            memberTravelType.getTravelType());
      }
    }

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("축제 설명");
  }

  @DisplayName("전통시장 추천 게시물 조회")
  @Test
  void findMarketRecommendPostDtoTest() {
    // given
    List<Recommend> recommendList = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // when
    RecommendPostDto recommendPostDto = null;
    for (Recommend recommend : recommendList) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      if (category.getContent().equals(CategoryContent.MARKET)) {
        recommendPostDto = recommendRepository.findMarketRecommendPostDto(
            postId, Locale.KOREAN,
            memberTravelType.getTravelType());
      }
    }

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("전통시장 설명");
  }

  @DisplayName("이색체험 추천 게시물 조회")
  @Test
  void findExperienceRecommendPostDtoTest() {
    // given
    List<Recommend> recommendList = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // when
    RecommendPostDto recommendPostDto = null;
    for (Recommend recommend : recommendList) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      if (category.getContent().equals(CategoryContent.EXPERIENCE)) {
        recommendPostDto = recommendRepository.findExperienceRecommendPostDto(
            postId, Locale.KOREAN,
            memberTravelType.getTravelType());
      }
    }

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("이색체험 설명");
  }

  @DisplayName("나나스픽 추천 게시물 조회")
  @Test
  void findNanaRecommendPostDtoTest() {
    // given
    List<Recommend> recommendList = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // when
    RecommendPostDto recommendPostDto = null;
    for (Recommend recommend : recommendList) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      if (category.getContent().equals(CategoryContent.NANA)) {
        recommendPostDto = recommendRepository.findNanaRecommendPostDto(
            postId, Locale.KOREAN,
            memberTravelType.getTravelType());
      }
    }

    // then
    Assertions.assertThat(recommendPostDto).extracting(RecommendPostDto::getIntroduction)
        .isEqualTo("나나 설명");
  }
}
