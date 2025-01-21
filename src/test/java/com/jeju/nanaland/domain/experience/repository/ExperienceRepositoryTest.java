package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.ART_MUSEUM;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.EXHIBITION;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.HISTORY;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.LAND_LEISURE;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.MUSEUM;
import static com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword.WATER_LEISURE;
import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceSearchDto;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceKeyword;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

  @Test
  @DisplayName("이색체험 상세조회")
  void findCompositeDtoByIdTest() {
    // given
    Language language = Language.KOREAN;
    // 지상레저 이색체험 게시물 1개 생성
    List<Experience> experienceList =
        getActivityList(language, List.of(LAND_LEISURE), "서귀포시", 1);

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
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(  // 액티비티 - 지상레저 2개
        getActivityList(language, List.of(LAND_LEISURE), "제주시", 2));
    experienceList.addAll(  // 액티비티 - 수상레저 3개
        getActivityList(language, List.of(WATER_LEISURE), "서귀포시", 3));
    experienceList.addAll(  // 문화예술 - 역사, 박물관 1개
        getCultureAndArtsList(language, List.of(HISTORY, MUSEUM), "제주시", 1));

    // when
    Page<ExperienceThumbnail> result = experienceRepository.findExperienceThumbnails(language,
        ExperienceType.ACTIVITY, List.of(LAND_LEISURE), List.of(), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
  }

  @Test
  @DisplayName("액티비티 리스트 조회 - 지역 필터")
  void findActivityThumbnailsWithAddressFilterTest() {
    // given
    Language language = Language.KOREAN;
    Pageable pageable = PageRequest.of(0, 12);
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(  // 액티비티 - 지상레저 2개
        getActivityList(language, List.of(LAND_LEISURE), "제주시", 2));
    experienceList.addAll(  // 액티비티 - 수상레저 2개
        getActivityList(language, List.of(WATER_LEISURE), "서귀포시", 2));
    experienceList.addAll(  // 문화예술 - 역사, 박물관 1개
        getCultureAndArtsList(language, List.of(HISTORY, MUSEUM), "제주시", 1));

    // when
    Page<ExperienceThumbnail> result = experienceRepository.findExperienceThumbnails(language,
        ExperienceType.ACTIVITY, List.of(), List.of(AddressTag.SEOGWIPO), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).extracting("addressTag")
        .containsOnly("서귀포시");
  }

  @Test
  @DisplayName("이색체험 키워드 조회")
  void getExperienceTypeKeywordSetTest() {
    // given
    Experience experience = getActivityList(Language.KOREAN,
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

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("액티비티 Union 검색")
  void findSearchDtoByKeywordsUnionActivityTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    List<Experience> experiences1 =
        getActivityList(language, List.of(LAND_LEISURE, WATER_LEISURE), "제주시", 2);
    initHashtags(experiences1, List.of("keyword1", "kEyWoRd2"), language);
    List<Experience> experiences2 =
        getCultureAndArtsList(language, List.of(EXHIBITION, MUSEUM, ART_MUSEUM), "서귀포시", 3);
    initHashtags(experiences2, List.of("keyword2", "kEyWoRd3"), language);

    // when
    Page<ExperienceSearchDto> resultDto = experienceRepository.findSearchDtoByKeywordsUnion(
        ExperienceType.ACTIVITY, List.of("keyword2", "keyword3"), language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(2);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(1);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("문화예술 Union 검색")
  void findSearchDtoByKeywordsUnionCultureAndArtsTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    List<Experience> experiences1 =
        getActivityList(language, List.of(LAND_LEISURE, WATER_LEISURE), "제주시", 2);
    initHashtags(experiences1, List.of("keyword1", "kEyWoRd2"), language);
    List<Experience> experiences2 =
        getCultureAndArtsList(language, List.of(EXHIBITION, MUSEUM, ART_MUSEUM), "서귀포시", 3);
    initHashtags(experiences2, List.of("keyword2", "kEyWoRd3"), language);

    // when
    Page<ExperienceSearchDto> resultDto = experienceRepository.findSearchDtoByKeywordsUnion(
        ExperienceType.CULTURE_AND_ARTS, List.of("keyword2", "keyword3"), language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(3);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("액티비티 Union 검색")
  void findSearchDtoByKeywordsIntersectActivityTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    List<String> keywords = List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5");
    List<Experience> experiences1 =
        getActivityList(language, List.of(LAND_LEISURE, WATER_LEISURE), "제주시", 2);
    initHashtags(experiences1, keywords, language);
    List<Experience> experiences2 =
        getCultureAndArtsList(language, List.of(EXHIBITION, MUSEUM, ART_MUSEUM), "서귀포시", 3);
    initHashtags(experiences2, keywords, language);

    // when
    Page<ExperienceSearchDto> resultDto = experienceRepository.findSearchDtoByKeywordsIntersect(
        ExperienceType.ACTIVITY, keywords, language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("문화예술 Union 검색")
  void findSearchDtoByKeywordsIntersectCultureAndArtsTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    List<String> keywords = List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5");
    List<Experience> experiences1 =
        getActivityList(language, List.of(LAND_LEISURE, WATER_LEISURE), "제주시", 2);
    initHashtags(experiences1, keywords, language);
    List<Experience> experiences2 =
        getCultureAndArtsList(language, List.of(EXHIBITION, MUSEUM, ART_MUSEUM), "서귀포시", 3);
    initHashtags(experiences2, keywords, language);

    // when
    Page<ExperienceSearchDto> resultDto = experienceRepository.findSearchDtoByKeywordsIntersect(
        ExperienceType.CULTURE_AND_ARTS, keywords, language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(3);
  }

  private List<Experience> getActivityList(Language language,
      List<ExperienceTypeKeyword> keywordList, String addressTag, int size) {
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
          .build();
      em.persistAndFlush(experience);
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("activity title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();
      em.persist(experienceTrans);

      for (ExperienceTypeKeyword keyword : keywordList) {
        ExperienceKeyword experienceKeyword = ExperienceKeyword.builder()
            .experienceTypeKeyword(keyword)
            .experience(experience)
            .build();
        em.persist(experienceKeyword);
      }

      experienceList.add(experience);
    }

    return experienceList;
  }

  private List<Experience> getCultureAndArtsList(Language language,
      List<ExperienceTypeKeyword> keywordList, String addressTag, int size) {
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
          .build();
      em.persistAndFlush(experience);
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("culture and arts title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();
      em.persist(experienceTrans);

      for (ExperienceTypeKeyword keyword : keywordList) {
        ExperienceKeyword experienceKeyword = ExperienceKeyword.builder()
            .experienceTypeKeyword(keyword)
            .experience(experience)
            .build();
        em.persist(experienceKeyword);
      }

      cultureAndArtsList.add(experience);
    }

    return cultureAndArtsList;
  }

  private void initHashtags(List<Experience> experiences, List<String> keywords,
      Language language) {
    List<Keyword> keywordList = new ArrayList<>();
    for (String k : keywords) {
      TypedQuery<Keyword> query = em.getEntityManager().createQuery(
          "SELECT k FROM Keyword k WHERE k.content = :keyword", Keyword.class);
      query.setParameter("keyword", k);
      List<Keyword> resultList = query.getResultList();

      if (resultList.isEmpty()) {
        Keyword newKeyword = Keyword.builder()
            .content(k)
            .build();
        em.persist(newKeyword);
        keywordList.add(newKeyword);
      } else {
        keywordList.add(resultList.get(0));
      }
    }

    for (Experience experience : experiences) {
      for (Keyword k : keywordList) {
        Hashtag newHashtag = Hashtag.builder()
            .post(experience)
            .category(Category.EXPERIENCE)
            .language(language)
            .keyword(k)
            .build();
        em.persist(newHashtag);
      }
    }
  }
}