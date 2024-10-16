package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.HISTORY;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.LAND_LEISURE;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.MUSEUM;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.WATER_LEISURE;
import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceKeyword;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExperienceRepositoryTest {

  @Autowired
  TestEntityManager em;

  @Autowired
  ExperienceRepository experienceRepository;

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("액티비티 상세조회")
  void findActivityExperienceCompositeDtoTest(Language language) {
    // given
    // 역사 키워드 문화예술 게시물 1개 생성
    Experience experience = createExperience(ExperienceType.ACTIVITY);
    ExperienceTrans experienceTrans = createExperienceTrans(experience, language,
        List.of(LAND_LEISURE), "제주시");
    Long postId = experience.getId();

    // when
    ExperienceCompositeDto result = experienceRepository.findExperienceCompositeDto(
        postId, language);

    // then
    assertThat(result.getTitle()).isEqualTo(experienceTrans.getTitle());
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("문화예술 상세조회")
  void findCultureAndArtsExperienceCompositeDtoTest(Language language) {
    // given
    // 역사 키워드 문화예술 게시물 1개 생성
    Experience experience = createExperience(ExperienceType.CULTURE_AND_ARTS);
    ExperienceTrans experienceTrans = createExperienceTrans(experience, language, List.of(HISTORY),
        "제주시");
    Long postId = experience.getId();

    // when
    ExperienceCompositeDto result = experienceRepository.findExperienceCompositeDto(
        postId, language);

    // then
    assertThat(result.getTitle()).isEqualTo(experienceTrans.getTitle());
  }

  @Test
  @DisplayName("액티비티 리스트 조회 - 키워드 필터")
  void findAllActivityPreviewDtosWithKeywordFilterTest() {
    // given
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    // 액티비티 - 지상레저 2개
    createActivities(language, List.of(LAND_LEISURE), "제주시", 2);
    // 액티비티 - 수상레저 3개
    createActivities(language, List.of(WATER_LEISURE), "서귀포시", 3);

    // when
    // 액티비티 - 지상레저
    Page<ExperienceResponse.PreviewDto> landLeisureResult =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.ACTIVITY, List.of(LAND_LEISURE), List.of(), pageable);
    // 액티비티 - 지상레저, 수상레저
    Page<ExperienceResponse.PreviewDto> landAndWaterLeisureResult =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.ACTIVITY, List.of(LAND_LEISURE, WATER_LEISURE), List.of(),
            pageable);

    // then
    assertThat(landLeisureResult.getTotalElements()).isEqualTo(2);
    assertThat(landAndWaterLeisureResult.getTotalElements()).isEqualTo(5);
  }

  @Test
  @DisplayName("액티비티 리스트 조회 - 지역 필터")
  void findAllActivityPreviewDtosWithAddressFilterTest() {
    // given
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    // 액티비티 - 지상레저 2개
    createActivities(language, List.of(LAND_LEISURE), "제주시", 2);
    // 액티비티 - 수상레저 3개
    createActivities(language, List.of(WATER_LEISURE), "서귀포시", 3);

    // when
    // 액티비티 - 지상레저
    Page<ExperienceResponse.PreviewDto> result1 =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.ACTIVITY, List.of(), List.of("제주시"), pageable);
    // 액티비티 - 지상레저, 수상레저
    Page<ExperienceResponse.PreviewDto> result2 =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.ACTIVITY, List.of(), List.of("제주시", "서귀포시"),
            pageable);

    // then
    assertThat(result1.getTotalElements()).isEqualTo(2);
    assertThat(result2.getTotalElements()).isEqualTo(5);
  }

  @Test
  @DisplayName("문화예술 리스트 조회 - 키워드 필터")
  void findAllCultureAndArtsPreviewDtosWithKeywordFilterTest() {
    // given
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    // 문화예술 - 역사 2개
    createCultureAndArts(language, List.of(HISTORY), "제주시", 2);
    // 문화예술 - 역사, 박물관 3개
    createCultureAndArts(language, List.of(HISTORY, MUSEUM), "서귀포시", 3);

    // when
    // 문화예술 - 역사
    Page<ExperienceResponse.PreviewDto> historyResult =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.CULTURE_AND_ARTS, List.of(HISTORY), List.of(), pageable);
    // 액티비티 - 박물관
    Page<ExperienceResponse.PreviewDto> museumResult =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.CULTURE_AND_ARTS, List.of(MUSEUM), List.of(), pageable);

    // then
    assertThat(historyResult.getTotalElements()).isEqualTo(5);
    assertThat(museumResult.getTotalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("문화예술 리스트 조회 - 지역 필터")
  void findAllCultureAndArtsPreviewDtosWithAddressFilterTest() {
    // given
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    // 문화예술 - 제주시 2개
    createCultureAndArts(language, List.of(HISTORY), "제주시", 2);
    // 문화예술 - 서귀포시 3개
    createCultureAndArts(language, List.of(HISTORY), "서귀포시", 3);

    // when
    // 문화예술 - 전체
    Page<ExperienceResponse.PreviewDto> result1 =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.CULTURE_AND_ARTS, List.of(), List.of(), pageable);
    // 문화예술 - 제주시
    Page<ExperienceResponse.PreviewDto> result2 =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.CULTURE_AND_ARTS, List.of(), List.of("제주시"), pageable);
    // 액티비티 - 서귀포시
    Page<ExperienceResponse.PreviewDto> result3 =
        experienceRepository.findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(
            language, ExperienceType.CULTURE_AND_ARTS, List.of(), List.of("서귀포시"), pageable);

    // then
    assertThat(result1.getTotalElements()).isEqualTo(5);
    assertThat(result2.getTotalElements()).isEqualTo(2);
    assertThat(result3.getTotalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("이색체험 키워드 조회")
  void getExperienceTypeKeywordSetTest() {
    // given
    Experience experience = createActivities(Language.KOREAN,
        List.of(LAND_LEISURE, WATER_LEISURE, HISTORY), "제주시", 1).get(0);

    // when
    Set<ExperienceTypeKeyword> keywordSet = experienceRepository.getExperienceTypeKeywordSet(
        experience.getId());

    // then
    assertThat(keywordSet).isEqualTo(
        Set.of(
            LAND_LEISURE,
            WATER_LEISURE,
            HISTORY
        )
    );
  }

  private ImageFile createImageFile() {
    ImageFile imageFile = ImageFile
        .builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
    em.persist(imageFile);

    return imageFile;
  }

  private Experience createExperience(ExperienceType experienceType) {
    ImageFile newImageFile = createImageFile();

    Experience newExperience = Experience.builder()
        .priority(0L)
        .firstImageFile(newImageFile)
        .experienceType(experienceType)
        .build();
    em.persist(newExperience);

    return newExperience;
  }

  private ExperienceTrans createExperienceTrans(Experience experience, Language language,
      List<ExperienceTypeKeyword> keywordList, String addressTag) {
    ExperienceTrans experienceTrans = ExperienceTrans.builder()
        .experience(experience)
        .language(language)
        .addressTag(addressTag)
        .title(UUID.randomUUID().toString())
        .build();
    em.persist(experienceTrans);

    for (ExperienceTypeKeyword keyword : keywordList) {
      ExperienceKeyword experienceKeyword = ExperienceKeyword.builder()
          .experienceTypeKeyword(keyword)
          .experience(experience)
          .build();
      em.persist(experienceKeyword);
    }

    return experienceTrans;
  }

  private List<Experience> createActivities(Language language,
      List<ExperienceTypeKeyword> keywordList, String addressTag, int size) {
    List<Experience> experienceList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      Experience experience = createExperience(ExperienceType.ACTIVITY);
      createExperienceTrans(experience, language, keywordList,
          addressTag);

      experienceList.add(experience);
    }

    return experienceList;
  }

  private List<Experience> createCultureAndArts(Language language,
      List<ExperienceTypeKeyword> keywordList, String addressTag, int size) {
    List<Experience> experienceList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      Experience experience = createExperience(ExperienceType.CULTURE_AND_ARTS);
      createExperienceTrans(experience, language, keywordList, addressTag);

      experienceList.add(experience);
    }

    return experienceList;
  }
}