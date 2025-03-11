package com.jeju.nanaland.domain.nature.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse;
import com.jeju.nanaland.domain.nature.dto.NatureSearchDto;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NatureRepositoryTest {

  @Autowired
  NatureRepository natureRepository;

  @Autowired
  TestEntityManager entityManager;

  private ImageFile createImageFile(Long number) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin" + number)
        .thumbnailUrl("thumbnail" + number)
        .build();
    entityManager.persist(imageFile);
    return imageFile;
  }

  private Nature createNature(Long priority) {
    Nature nature = Nature.builder()
        .firstImageFile(createImageFile(priority))
        .priority(priority)
        .build();
    entityManager.persist(nature);
    return nature;
  }

  private NatureTrans createNatureTrans(Nature nature, int number, String keyword,
      String addressTag, String address) {
    NatureTrans natureTrans = NatureTrans.builder()
        .nature(nature)
        .language(Language.KOREAN)
        .title(keyword + "title" + number)
        .content("content" + number)
        .addressTag(addressTag)
        .address(address)
        .build();
    entityManager.persist(natureTrans);
    return natureTrans;
  }

  private void createNatureItems(int itemCount, String keyword, String addressTag) {
    for (int i = 1; i <= itemCount; i++) {
      Nature nature = createNature((long) i);
      createNatureTrans(nature, i, keyword, addressTag, "주소");
    }
  }

  private void initHashtags(List<Nature> natures, List<String> keywords,
      Language language) {
    List<Keyword> keywordList = new ArrayList<>();
    for (String k : keywords) {
      TypedQuery<Keyword> query = entityManager.getEntityManager().createQuery(
          "SELECT k FROM Keyword k WHERE k.content = :keyword", Keyword.class);
      query.setParameter("keyword", k);
      List<Keyword> resultList = query.getResultList();

      if (resultList.isEmpty()) {
        Keyword newKeyword = Keyword.builder()
            .content(k)
            .build();
        entityManager.persist(newKeyword);
        keywordList.add(newKeyword);
      } else {
        keywordList.add(resultList.get(0));
      }
    }

    for (Nature nature : natures) {
      for (Keyword k : keywordList) {
        Hashtag newHashtag = Hashtag.builder()
            .post(nature)
            .category(Category.NATURE)
            .language(language)
            .keyword(k)
            .build();
        entityManager.persist(newHashtag);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("키워드 4개 이하 검색")
  void findSearchDtoByKeywordsUnionTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 12);
    int size = 3;
    for (int i = 0; i < size; i++) {
      Nature nature = createNature((long) i);
      NatureTrans natureTrans = createNatureTrans(nature, i, "test", "제주시", "주소");
      initHashtags(List.of(nature), List.of("keyword" + i, "keyword" + (i + 1)), language);
    }

    // when
    Page<NatureSearchDto> resultDto = natureRepository.findSearchDtoByKeywordsUnion(
        List.of("keyword1", "keyword2"), null, language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(3);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("키워드 5개 이상 검색")
  void findSearchDtoByKeywordsIntersectTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 12);
    int size = 3;
    for (int i = 0; i < size; i++) {
      Nature nature = createNature((long) i);
      NatureTrans natureTrans = createNatureTrans(nature, i, "test", "제주시", "주소");
      initHashtags(List.of(nature),
          List.of("keyword" + i, "keyword" + (i + 1), "keyword" + (i + 2), "keyword" + (i + 3),
              "keyword" + (i + 4)),
          language);
    }

    // when
    Page<NatureSearchDto> resultDto = natureRepository.findSearchDtoByKeywordsIntersect(
        List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5"), null, language,
        pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(1);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(5);
  }

  @Test
  @DisplayName("7대 자연 정보 조회 TEST")
  void findNatureCompositeDto() {
    // given: 7대 자연 정보 설정
    Nature nature = createNature(0L);
    NatureTrans natureTrans = createNatureTrans(nature, 0, "", "제주시", "주소");

    // when: 7대 자연 정보 조회
    NatureCompositeDto result = natureRepository.findNatureCompositeDto(nature.getId(),
        Language.KOREAN);

    // then: 7대 자연 정보 검증
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(nature.getId());
    assertThat(result.getTitle()).isEqualTo(natureTrans.getTitle());
    assertThat(result.getContent()).isEqualTo(natureTrans.getContent());
  }

  @Nested
  @DisplayName("7대 자연 프리뷰 페이징 조회 TEST")
  class findAllNaturePreviewDtoOrderByCreatedAt {

    @Test
    @DisplayName("기본 케이스")
    void findAllNaturePreviewDtoOrderByPriority_basic() {
      // given: 7대 자연 리스트 설정
      int itemCount = 5;
      createNatureItems(itemCount, "", "제주시");
      Pageable pageable = PageRequest.of(0, 12);

      // when: 7대 자연 프리뷰 페이징 조회
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(
          Language.KOREAN, new ArrayList<>(), "", pageable);

      // then: 7대 자연 프리뷰 페이징 검증
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(itemCount - 1);
      assertThat(firstItem.getTitle()).isEqualTo("title" + itemCount);
      assertThat(firstItem.getFirstImage().getOriginUrl()).isEqualTo("origin" + itemCount);
      assertThat(firstItem.getFirstImage().getThumbnailUrl()).isEqualTo("thumbnail" + itemCount);
      assertThat(firstItem.getAddressTag()).isEqualTo("제주시");
    }

    @Test
    @DisplayName("키워드 검색")
    void findAllNaturePreviewDtoOrderByPriority_keyword() {
      // given: keyword가 포함되도록 7대 자연 설정
      int itemCount = 6;
      String keyword = "여행";
      createNatureItems(itemCount, keyword, "제주시");
      Pageable pageable = PageRequest.of(0, 12);

      // when: 7대 자연 프리뷰 페이징 조회
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(
          Language.KOREAN, new ArrayList<>(), keyword, pageable);

      // then: 7대 자연 프리뷰 페이징 검증, 키워드 포함되어 있는지 확인
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(itemCount - 1);
      assertThat(firstItem.getTitle()).isEqualTo(keyword + "title" + itemCount);
      assertThat(firstItem.getFirstImage().getOriginUrl()).isEqualTo("origin" + itemCount);
      assertThat(firstItem.getFirstImage().getThumbnailUrl()).isEqualTo("thumbnail" + itemCount);
      assertThat(firstItem.getAddressTag()).isEqualTo("제주시");
    }

    @Test
    @DisplayName("지역명 필터")
    void findAllNaturePreviewDtoOrderByPriority_addressFilter() {
      // given: 지역명 포함하여 7대 자연 설정
      int itemCount = 7;
      AddressTag addressTag = AddressTag.AEWOL;
      createNatureItems(itemCount, "", addressTag.getKr());
      Pageable pageable = PageRequest.of(0, 12);

      // when: 7대 자연 프리뷰 페이징 조회
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(
          Language.KOREAN, List.of(addressTag), "", pageable);

      // then: 7대 자연 프리뷰 페이징 검증, 지역필터 올바른지 확인
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(itemCount - 1);
      assertThat(firstItem.getTitle()).isEqualTo("title" + itemCount);
      assertThat(firstItem.getFirstImage().getOriginUrl()).isEqualTo("origin" + itemCount);
      assertThat(firstItem.getFirstImage().getThumbnailUrl()).isEqualTo("thumbnail" + itemCount);
      assertThat(firstItem.getAddressTag()).isEqualTo(addressTag.getKr());
    }

    @Test
    @DisplayName("자연 한국어 주소 조회")
    void findKoreanAddressTest() {
      // given
      Nature nature = createNature(0L);
      createNatureTrans(nature, 1, "test", "제주시", "주소");

      // when
      Optional<String> koreanAddress = natureRepository.findKoreanAddress(nature.getId());

      // then
      assertThat(koreanAddress.get()).isEqualTo("주소");
    }

    @Test
    @DisplayName("주소가 null인 경우 한국어 주소 조회")
    void findKoreanAddressFailedTest() {
      // given - 주소가 null
      Nature nature = createNature(0L);
      createNatureTrans(nature, 1, "test", "제주시", null);

      // when
      Optional<String> koreanAddress = natureRepository.findKoreanAddress(nature.getId());

      // then
      assertThat(koreanAddress.isPresent()).isFalse();
    }
  }
}