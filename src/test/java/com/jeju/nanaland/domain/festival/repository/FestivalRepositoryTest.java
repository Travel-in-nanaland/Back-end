package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    Page<FestivalCompositeDto> festivalCompositeDtoPage = festivalRepository.searchCompositeDtoByOnGoing(
        Language.KOREAN, PageRequest.of(0, 5), true, new ArrayList<>());

    List<FestivalCompositeDto> onGoingFestivalWithoutAddressFilter = festivalCompositeDtoPage.getContent();

    List<FestivalCompositeDto> onGoingFestivalWithAddressFilter = festivalRepository.searchCompositeDtoByOnGoing(
        Language.KOREAN, PageRequest.of(0, 5), true, new ArrayList<>(List.of("제주시"))).getContent();

    List<FestivalCompositeDto> finishFestivalWithoutAddressFilter = festivalRepository.searchCompositeDtoByOnGoing(
        Language.KOREAN, PageRequest.of(0, 5), false, new ArrayList<>()).getContent();

    List<FestivalCompositeDto> finishFestivalWithAddressFilter = festivalRepository.searchCompositeDtoByOnGoing(
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
    Page<FestivalCompositeDto> springFestivalPage = festivalRepository.searchCompositeDtoBySeason(
        Language.KOREAN, PageRequest.of(0, 5), "봄");
    List<FestivalCompositeDto> springFestival = springFestivalPage.getContent();
    List<FestivalCompositeDto> summerFestival = festivalRepository.searchCompositeDtoBySeason(
        Language.KOREAN, PageRequest.of(0, 5), "여름").getContent();
    List<FestivalCompositeDto> autumnFestival = festivalRepository.searchCompositeDtoBySeason(
        Language.KOREAN, PageRequest.of(0, 5), "가을").getContent();
    List<FestivalCompositeDto> winterFestival = festivalRepository.searchCompositeDtoBySeason(
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
    Page<FestivalCompositeDto> allFestivalPage = festivalRepository.searchCompositeDtoByMonth(
        Language.KOREAN, PageRequest.of(0, 5),
        LocalDate.of(1999, 1, 1), LocalDate.of(2040, 1, 1), new ArrayList<>());
    List<FestivalCompositeDto> allFestival = allFestivalPage.getContent();
    List<FestivalCompositeDto> festivalByDate = festivalRepository.searchCompositeDtoByMonth(
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
    imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    imageFile2 = TestUtil.findImageFileByNumber(em, 2);

    imageFile3 = TestUtil.findImageFileByNumber(em, 3);

    imageFile4 = TestUtil.findImageFileByNumber(em, 4);

    imageFile5 = TestUtil.findImageFileByNumber(em, 5);

    language = Language.KOREAN;

    festival1 = TestUtil.findFestivalByStringSeason(em, "봄,여름,가을,겨울");

    festival2 = TestUtil.findFestivalByStringSeason(em, "가을");

    festival3 = TestUtil.findFestivalByStringSeason(em, "겨울");

    festival4 = TestUtil.findFestivalByStringSeason(em, "봄,여름");

    festival5 = TestUtil.findFestivalByStringSeason(em, "봄,겨울");

    festivalTrans1 = TestUtil.findFestivalTransByFestival(em, festival1);

    festivalTrans2 = TestUtil.findFestivalTransByFestival(em, festival2);

    festivalTrans3 = TestUtil.findFestivalTransByFestival(em, festival3);

    festivalTrans4 = TestUtil.findFestivalTransByFestival(em, festival4);

    festivalTrans5 = TestUtil.findFestivalTransByFestival(em, festival5);


  }
}