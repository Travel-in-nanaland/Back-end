package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FestivalRepositoryTest {

  @Autowired
  FestivalRepository festivalRepository;
  Festival festival1, festival2, festival3, festival4, festival5;
  FestivalTrans festivalTrans1, festivalTrans2, festivalTrans3, festivalTrans4, festivalTrans5;
  ImageFile imageFile1, imageFile2, imageFile3, imageFile4, imageFile5;
  Language language;
  @PersistenceContext
  private EntityManager em;

  @Test
  @DisplayName("축제 검색")
  void searchFestivalTest() {
    Pageable pageable = PageRequest.of(0, 12);
    festivalRepository.searchCompositeDtoByKeyword("쇼핑", Language.KOREAN, pageable);
  }

  @Test
  @DisplayName("진행 중인 축제 검색")
  void searchCompositeDtoByOnGoing() {
    // Given
    setFestival();

    // When
    Page<FestivalCompositeDto> festivalCompositeDtoPage = festivalRepository.findAllFestivalCompositDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), true, new ArrayList<>());

    List<FestivalCompositeDto> onGoingFestivalWithoutAddressFilter = festivalCompositeDtoPage.getContent();

    List<FestivalCompositeDto> onGoingFestivalWithAddressFilter = festivalRepository.findAllFestivalCompositDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), true, new ArrayList<>(List.of("제주시"))).getContent();

    List<FestivalCompositeDto> finishFestivalWithoutAddressFilter = festivalRepository.findAllFestivalCompositDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), false, new ArrayList<>()).getContent();

    List<FestivalCompositeDto> finishFestivalWithAddressFilter = festivalRepository.findAllFestivalCompositDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), false, new ArrayList<>(List.of("한림"))).getContent();

    // Then
    Assertions.assertThat(festivalCompositeDtoPage.getTotalElements()).isEqualTo(3);
    Assertions.assertThat(festivalCompositeDtoPage.getTotalPages()).isEqualTo(1);

    Assertions.assertThat(onGoingFestivalWithoutAddressFilter.size()).isEqualTo(3);
    Assertions.assertThat(onGoingFestivalWithAddressFilter.size()).isEqualTo(2);

    Assertions.assertThat(finishFestivalWithoutAddressFilter.size()).isEqualTo(2);
    Assertions.assertThat(finishFestivalWithAddressFilter.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("계절 별 축제 검색")
  void searchCompositeDtoBySeason() {
    // Given
    setFestival();

    // When
    Page<FestivalCompositeDto> springFestivalPage = festivalRepository.findAllFestivalCompositeDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), "봄");
    List<FestivalCompositeDto> springFestival = springFestivalPage.getContent();
    List<FestivalCompositeDto> summerFestival = festivalRepository.findAllFestivalCompositeDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), "여름").getContent();
    List<FestivalCompositeDto> autumnFestival = festivalRepository.findAllFestivalCompositeDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), "가을").getContent();
    List<FestivalCompositeDto> winterFestival = festivalRepository.findAllFestivalCompositeDtoOrderByEndDate(
        Language.KOREAN, PageRequest.of(0, 5), "겨울").getContent();

    // Then
    Assertions.assertThat(springFestivalPage.getTotalElements()).isEqualTo(3);
    Assertions.assertThat(springFestivalPage.getTotalPages()).isEqualTo(1);

    Assertions.assertThat(springFestival.size()).isEqualTo(3);
    Assertions.assertThat(summerFestival.size()).isEqualTo(2);
    Assertions.assertThat(autumnFestival.size()).isEqualTo(2);
    Assertions.assertThat(springFestival.size()).isEqualTo(3);
  }

  @Test
  @DisplayName("월 별 축제 조회")
  void searchCompositeDtoByMonth() {
    // Given
    setFestival();

    // When
    Page<FestivalCompositeDto> allFestivalPage = festivalRepository.findAllFestivalCompositeDtoByEndDate(
        Language.KOREAN, PageRequest.of(0, 5),
        LocalDate.of(1999, 1, 1), LocalDate.of(2040, 1, 1), new ArrayList<>());
    List<FestivalCompositeDto> allFestival = allFestivalPage.getContent();
    List<FestivalCompositeDto> festivalByDate = festivalRepository.findAllFestivalCompositeDtoByEndDate(
        Language.KOREAN, PageRequest.of(0, 5),
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 13), new ArrayList<>()).getContent();

    // Then
    Assertions.assertThat(allFestivalPage.getTotalElements()).isEqualTo(5);
    Assertions.assertThat(allFestivalPage.getTotalPages()).isEqualTo(1);

    Assertions.assertThat(allFestival.size()).isEqualTo(5);
    Assertions.assertThat(festivalByDate.size()).isEqualTo(3);

  }

  /**
   * festival1 : 진행중, 봄 여름 가을 겨울, 제주시 / festival2 : 진행중, 가을, 서귀포시 festival3 : 진행중, 겨울, 제주시 /
   * festival4 : 종료, 봄 여름, 표선 festival5 : 종료, 봄 겨울, 한림
   */

  private void setFestival() {
    imageFile1 = ImageFile.builder()
        .originUrl("originUrl1")
        .thumbnailUrl("thumbnailUrl1")
        .build();
    em.persist(imageFile1);

    imageFile2 = ImageFile.builder()
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
        .build();
    em.persist(imageFile2);

    imageFile3 = ImageFile.builder()
        .originUrl("originUrl3")
        .thumbnailUrl("thumbnailUrl3")
        .build();
    em.persist(imageFile3);

    imageFile4 = ImageFile.builder()
        .originUrl("originUrl4")
        .thumbnailUrl("thumbnailUrl4")
        .build();
    em.persist(imageFile4);

    imageFile5 = ImageFile.builder()
        .originUrl("originUrl5")
        .thumbnailUrl("thumbnailUrl5")
        .build();
    em.persist(imageFile5);

    language = Language.KOREAN;

    festival1 = Festival.builder()
        .firstImageFile(imageFile1)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2028, 3, 1))
        .season("봄,여름,가을,겨울")
        .priority(0L)
        .build();
    em.persist(festival1);

    festival2 = Festival.builder()
        .firstImageFile(imageFile2)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2028, 3, 2))
        .season("가을")
        .priority(0L)
        .build();
    em.persist(festival2);

    festival3 = Festival.builder()
        .firstImageFile(imageFile3)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2026, 3, 3))
        .season("겨울")
        .priority(0L)
        .build();
    em.persist(festival3);

    festival4 = Festival.builder()
        .firstImageFile(imageFile4)
        .onGoing(false)
        .startDate(LocalDate.of(2022, 3, 10))
        .endDate(LocalDate.of(2023, 3, 4))
        .season("봄,여름")
        .priority(0L)
        .build();
    em.persist(festival4);

    festival5 = Festival.builder()
        .firstImageFile(imageFile5)
        .onGoing(false)
        .startDate(LocalDate.of(2000, 4, 10))
        .endDate(LocalDate.of(2002, 3, 5))
        .season("봄,겨울")
        .priority(0L)
        .build();
    em.persist(festival5);

    festivalTrans1 = FestivalTrans.builder()
        .festival(festival1)
        .language(language)
        .address("제주특별자치도 제주시 조함해안로 525함덕해수욕장 일원")
        .addressTag("제주시")
        .build();
    em.persist(festivalTrans1);

    festivalTrans2 = FestivalTrans.builder()
        .festival(festival2)
        .language(language)
        .address("제주특별자치도 서귀포시 중정로 22")
        .addressTag("서귀포시")
        .build();
    em.persist(festivalTrans2);

    festivalTrans3 = FestivalTrans.builder()
        .festival(festival3)
        .language(language)
        .address("제주특별자치도 제주시 동광로 90(이도이동)")
        .addressTag("제주시")
        .build();
    em.persist(festivalTrans3);

    festivalTrans4 = FestivalTrans.builder()
        .festival(festival4)
        .language(language)
        .address("제주특별자치도 서귀포시 표선면 녹산로 381-17")
        .addressTag("표선")
        .build();
    em.persist(festivalTrans4);

    festivalTrans5 = FestivalTrans.builder()
        .festival(festival5)
        .language(language)
        .address("제주특별자치도 제주시 한림읍 한림로 300(한림읍)")
        .addressTag("한림")
        .build();
    em.persist(festivalTrans5);

  }
}