package com.jeju.nanaland.domain.experience.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
class ExperienceRepositoryTest {

  @Autowired
  TestEntityManager em;

  @Autowired
  ExperienceRepository experienceRepository;

  @Test
  @DisplayName("이색체험 상세조회")
  void findCompositeDtoByIdTest() {
    // given
    Language language = Language.KOREAN;
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(getActivityList(language, "수상레저", "서귀포시", 1));

    // when
    ExperienceCompositeDto result = experienceRepository.findCompositeDtoById(
        experienceList.get(0).getId(), language);

    // then
    assertThat(result.getTitle()).isEqualTo("activity title 1");
  }

  @Test
  @DisplayName("액티비티 리스트 조회 - 키워드 필터")
  void findActivityThumbnailsWithKeywordFilterTest() {
    // given
    ExperienceType experienceType = ExperienceType.ACTIVITY;
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(  // 액티비티 - 지상레저 2개
        getActivityList(language, "지상레저", "제주시", 2));
    experienceList.addAll(  // 액티비티 - 수상레저 2개
        getActivityList(language, "수상레저", "서귀포시", 2));
    experienceList.addAll(  // 문화예술 - 박물관 1개
        getCultureAndArtsList(language, "박물관", "제주시", 1));

    // when
    Page<ExperienceThumbnail> result = experienceRepository.findExperienceThumbnails(language,
        ExperienceType.ACTIVITY, List.of("지상레저"), List.of(), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
  }

  @Test
  @DisplayName("액티비티 리스트 조회 - 지역 필터")
  void findActivityThumbnailsWithAddressFilterTest() {
    // given
    ExperienceType experienceType = ExperienceType.ACTIVITY;
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(  // 액티비티 - 지상레저 2개
        getActivityList(language, "지상레저", "제주시", 2));
    experienceList.addAll(  // 액티비티 - 수상레저 2개
        getActivityList(language, "수상레저", "서귀포시", 2));
    experienceList.addAll(  // 문화예술 - 박물관 1개
        getCultureAndArtsList(language, "박물관", "제주시", 1));

    // when
    Page<ExperienceThumbnail> result = experienceRepository.findExperienceThumbnails(language,
        ExperienceType.ACTIVITY, List.of(), List.of("서귀포시"), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).extracting("addressTag")
        .containsOnly("서귀포시");
  }

  private List<Experience> getActivityList(Language language, String keyword, String addressTag,
      int size) {
    List<Experience> experienceList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("originUrl" + i)
          .thumbnailUrl("thumbnailUrl" + i)
          .build();
      em.persist(imageFile);
      Experience experience = Experience.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .experienceType(ExperienceType.ACTIVITY)
          .keywords(keyword)
          .build();
      em.persistAndFlush(experience);
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("activity title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();
      em.persist(experienceTrans);
      experienceList.add(experience);
    }

    return experienceList;
  }

  private List<Experience> getCultureAndArtsList(Language language, String keyword,
      String addressTag, int size) {
    List<Experience> cultureAndArtsList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("originUrl" + i)
          .thumbnailUrl("thumbnailUrl" + i)
          .build();
      em.persist(imageFile);
      Experience experience = Experience.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .experienceType(ExperienceType.CULTURE_AND_ARTS)
          .keywords(keyword)
          .build();
      em.persistAndFlush(experience);
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("culture and arts title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();
      em.persist(experienceTrans);
      cultureAndArtsList.add(experience);
    }

    return cultureAndArtsList;
  }
}