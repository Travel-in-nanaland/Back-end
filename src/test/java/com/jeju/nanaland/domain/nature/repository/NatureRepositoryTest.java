package com.jeju.nanaland.domain.nature.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  private NatureTrans createNatureTrans(Nature nature, int number, String keyword, String addressTag) {
    NatureTrans natureTrans = NatureTrans.builder()
        .nature(nature)
        .language(Language.KOREAN)
        .title(keyword + "title" + number)
        .content("content" + number)
        .addressTag(addressTag)
        .build();
    entityManager.persist(natureTrans);
    return natureTrans;
  }

  private void createNatureItems(int itemCount, String keyword, String addressTag) {
    for (int i = 1; i <= itemCount; i++) {
      Nature nature = createNature((long) i);
      createNatureTrans(nature, i, keyword, addressTag);
    }
  }

  @Test
  @DisplayName("7대 자연 검색 TEST")
  void searchNatureTest() {
    Pageable pageable = PageRequest.of(0, 12);
    natureRepository.searchCompositeDtoByKeyword("자연경관", Language.KOREAN, pageable);
  }

  @Test
  @DisplayName("7대 자연 정보 조회 TEST")
  void findNatureCompositeDto() {
    // given: 7대 자연 정보 설정
    Nature nature = createNature(0L);
    NatureTrans natureTrans = createNatureTrans(nature, 0, "", "제주시");

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
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriority(
          Language.KOREAN, new ArrayList<>(), "", pageable);

      // then: 7대 자연 프리뷰 페이징 검증
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(0);
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
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriority(
          Language.KOREAN, new ArrayList<>(), keyword, pageable);

      // then: 7대 자연 프리뷰 페이징 검증, 키워드 포함되어 있는지 확인
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(0);
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
      String addressTag = "애월";
      createNatureItems(itemCount, "", addressTag);
      Pageable pageable = PageRequest.of(0, 12);

      // when: 7대 자연 프리뷰 페이징 조회
      Page<NatureResponse.PreviewDto> result = natureRepository.findAllNaturePreviewDtoOrderByPriority(
          Language.KOREAN, List.of(addressTag), "", pageable);

      // then: 7대 자연 프리뷰 페이징 검증, 지역필터 올바른지 확인
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(itemCount);
      assertThat(result.getTotalElements()).isEqualTo(itemCount);

      NatureResponse.PreviewDto firstItem = result.getContent().get(0);
      assertThat(firstItem.getTitle()).isEqualTo("title" + itemCount);
      assertThat(firstItem.getFirstImage().getOriginUrl()).isEqualTo("origin" + itemCount);
      assertThat(firstItem.getFirstImage().getThumbnailUrl()).isEqualTo("thumbnail" + itemCount);
      assertThat(firstItem.getAddressTag()).isEqualTo(addressTag);
    }
  }
}