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
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.RecommendTrans;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
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
    em.persist(natureCategory);
    em.persist(experienceCategory);

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
        .title("nature title")
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
        .title("festival title")
        .language(korean)
        .festival(festival1)
        .build();
    em.persist(festivalTrans1);

    // experience, experienceTrans init
    Experience experience1 = Experience.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(experience1);
    ExperienceTrans experienceTrans1 = ExperienceTrans.builder()
        .title("experience title")
        .experience(experience1)
        .language(korean)
        .build();
    em.persist(experienceTrans1);

    // nana, nanaTitle init
    Nana.builder()
        .build();

    // recommend, recommendTrans init
    Recommend recommend1 = Recommend.builder()
        .memberTravelType(memberTravelType)
        .postId(nature1.getId())
        .category(natureCategory)
        .build();
    em.persist(recommend1);
    RecommendTrans recommendTrans1 = RecommendTrans.builder()
        .recommend(recommend1)
        .introduction("7대자연 설명 1")
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
        .introduction("이색체험 설명 1")
        .language(korean)
        .build();
    em.persist(recommendTrans2);
  }

  @DisplayName("타입을 통해 추천 게시물 조회")
  @Test
  void findAllByMemberTravelTypeTest() {
    // given
    // when
    List<Recommend> result = recommendRepository.findAllByMemberTravelType(memberTravelType);

    // then
    Assertions.assertThat(result.size()).isEqualTo(2);
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
        .isEqualTo("7대자연 설명 1");
  }
}
