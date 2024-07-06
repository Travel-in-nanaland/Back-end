package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaContentImage;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NanaRepositoryImplTest {

  Language language, language2;
  ImageFile imageFile1, imageFile2, imageFile3, imageFile4, imageFile5;
  Nana nana1, nana2, nana3, nana4, nana5;
  NanaTitle nanaTitle1, nanaTitle2, nanaTitle3, nanaTitle4, nanaTitle5;
  NanaContent nanaContent1, nanaContent2, nanaContent3;
  @PersistenceContext
  private EntityManager em;
  @Autowired
  private NanaRepositoryImpl nanarepositoryImpl;


  // nana 5개, 각 nana에 nanaTitle 1개(nana 1개당 nanaTitle 1개씩 -korean만),nanaTitle1에 nanaContent 3개
  private void setNana() {
    language = Language.KOREAN;

    language2 = Language.CHINESE;

    imageFile1 = ImageFile.builder()
        .originUrl("originUrl1")
        .thumbnailUrl("thumbnailUrl1")
        .build();
    em.persist(imageFile1);

    imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    imageFile2 = TestUtil.findImageFileByNumber(em, 2);

    imageFile3 = TestUtil.findImageFileByNumber(em, 3);

    imageFile4 = TestUtil.findImageFileByNumber(em, 4);

    imageFile5 = TestUtil.findImageFileByNumber(em, 5);

    nana1 = TestUtil.findNana(em, 1);

    nana2 = TestUtil.findNana(em, 2);

    nana3 = TestUtil.findNana(em, 3);

    nana4 = TestUtil.findNana(em, 4);

    nana5 = TestUtil.findNana(em, 5);

    nanaTitle1 = TestUtil.findNanaTitleByNana(em, nana1);

    //nana3=> active = true / language2=> chinese
    nanaTitle2 = TestUtil.findNanaTitleByNana(em, nana2);

    nanaTitle3 = TestUtil.findNanaTitleByNana(em, nana3);

    nanaTitle4 = TestUtil.findNanaTitleByNana(em, nana4);

    nanaTitle5 = TestUtil.findNanaTitleByNana(em, nana5);

    nanaContent1 = TestUtil.findNanaContentByNanaTitleAndNumber(em, nanaTitle1, 1);

    nanaContent2 = TestUtil.findNanaContentByNanaTitleAndNumber(em, nanaTitle1, 2);

    nanaContent3 = TestUtil.findNanaContentByNanaTitleAndNumber(em, nanaTitle1, 3);

    NanaContentImage nanaContentImage1 = NanaContentImage.builder()
        .imageFile(imageFile1)
        .nana(nana1)
        .number(1)
        .build();

    NanaContentImage nanaContentImage2 = NanaContentImage.builder()
        .imageFile(imageFile2)
        .nana(nana1)
        .number(2)
        .build();

    NanaContentImage nanaContentImage3 = NanaContentImage.builder()
        .imageFile(imageFile3)
        .nana(nana1)
        .number(3)
        .build();

    nana1.updateNanaContentImageList(
        List.of(nanaContentImage1, nanaContentImage2, nanaContentImage3));
  }

  @Test
  @DisplayName("나나's pick 베너 조회 시 최근에 추가된 4개의 나나가 나온다.")
  void findRecentNanaThumbnailDto() {
    // Given
    setNana();

    // When
    List<NanaThumbnail> recentNanaThumbnailDto = nanarepositoryImpl.findRecentNanaThumbnailDto(
        Language.KOREAN);

    // Then
    Assertions.assertThat(recentNanaThumbnailDto.get(0).getId())
        .isEqualTo(nana5.getId());
  }

  @Test
  @DisplayName("나나's pick 전체 리스트 조회 시 최신순으로 보여진다.")
  void findAllNanaThumbnailDto() {
    // Given
    setNana();

    // When
    Page<NanaThumbnail> allNanaThumbnailDto = nanarepositoryImpl.findAllNanaThumbnailDto(
        Language.KOREAN,
        PageRequest.of(0, 12));
    List<NanaThumbnail> result = allNanaThumbnailDto.getContent();

    // Then
    Assertions.assertThat(result.get(0).getId()).isEqualTo(nana5.getId());
    Assertions.assertThat(result.get(result.size() - 1).getId()).isEqualTo(nana1.getId());
  }

  @Test
  void searchNanaThumbnailDtoByKeyword() {
    // Given
    setNana();
    System.out.println("nanaTitle2.getCreatedAt " + nanaTitle2.getCreatedAt());
    // When
    Page<NanaThumbnail> keyword = nanarepositoryImpl.searchNanaThumbnailDtoByKeyword("keyword",
        Language.KOREAN, PageRequest.of(0, 12));
    List<NanaThumbnail> content = keyword.getContent();
    for (NanaThumbnail nanaThumbnail : content) {
      System.out.println("nanaThumbnail = " + nanaThumbnail.toString());
    }

    // Then
    boolean isSearched = false;
    for (NanaThumbnail nanaThumbnail : content) {
      if (nanaThumbnail.getSubHeading().equals("keyword")) {
        isSearched = true;
      }
    }
    Assertions.assertThat(isSearched).isTrue();
  }

  @Test
  void findNanaThumbnailPostDto() {
    // Given
    setNana();

    // When
    NanaThumbnailPost nanaThumbnailPostDto = nanarepositoryImpl.findNanaThumbnailPostDto(
        nanaTitle3.getId(), Language.KOREAN);

    // Then
    Assertions.assertThat(nanaThumbnailPostDto.getId()).isEqualTo(nanaTitle3.getId());
  }
}