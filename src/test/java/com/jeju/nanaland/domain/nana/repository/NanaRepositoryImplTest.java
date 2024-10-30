package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.PostImageFile;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import jakarta.persistence.EntityManager;
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
  @Autowired
  private EntityManager em;
  @Autowired
  private NanaRepositoryImpl nanaRepositoryImpl;


  // nana 5개, 각 nana에 nanaTitle 1개(nana 1개당 nanaTitle 1개씩 -korean만),nanaTitle1에 nanaContent 3개
  private void setNana() {
    language = Language.KOREAN;

    language2 = Language.CHINESE;

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
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
        .build();
    em.persist(imageFile4);

    imageFile5 = ImageFile.builder()
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
        .build();
    em.persist(imageFile5);

    nana1 = Nana.builder()
        .version("ver1")
        .firstImageFile(imageFile1)
        .priority(0L)
        .build();
    em.persist(nana1);

    nana2 = Nana.builder()
        .version("ver2")
        .firstImageFile(imageFile2)
        .priority(0L)
        .build();
    em.persist(nana2);

    nana3 = Nana.builder()
        .version("ver3")
        .firstImageFile(imageFile3)
        .priority(0L)
        .build();
    em.persist(nana3);

    nana4 = Nana.builder()
        .version("ver4")
        .firstImageFile(imageFile4)
        .priority(0L)
        .build();
    em.persist(nana4);

    nana5 = Nana.builder()
        .version("ver5")
        .firstImageFile(imageFile5)
        .priority(0L)
        .build();
    em.persist(nana5);

    nanaTitle1 = NanaTitle.builder()
        .notice("notice1")
        .language(language)
        .nana(nana1)
        .build();
    em.persist(nanaTitle1);

    //nana3=> active = true / language2=> chinese
    nanaTitle2 = NanaTitle.builder()
        .notice("notice2")
        .heading("keyword")
        .language(language)
        .nana(nana2)
        .build();
    em.persist(nanaTitle2);

    nanaTitle3 = NanaTitle.builder()
        .notice("notice3")
        .language(language)
        .nana(nana3)
        .build();
    em.persist(nanaTitle3);

    nanaTitle4 = NanaTitle.builder()
        .notice("notice4")
        .language(language)
        .nana(nana4)
        .build();
    em.persist(nanaTitle4);
    nanaTitle5 = NanaTitle.builder()
        .notice("notice5")
        .language(language)
        .nana(nana5)
        .build();
    em.persist(nanaTitle5);

    nanaContent1 = NanaContent.builder()
        .subTitle("subtitle1")
        .nanaTitle(nanaTitle1)
        .content("content")
        .priority(1L)
        .title("title")
        .build();
    em.persist(nanaContent1);

    nanaContent2 = NanaContent.builder()
        .subTitle("subtitle2")
        .nanaTitle(nanaTitle1)
        .content("content2")
        .priority(2L)
        .title("title2")
        .build();
    em.persist(nanaContent2);

    nanaContent3 = NanaContent.builder()
        .subTitle("subtitle3")
        .nanaTitle(nanaTitle1)
        .content("content3")
        .priority(3L)
        .title("title3")
        .build();
    em.persist(nanaContent3);

    PostImageFile postImageFile1 = PostImageFile.builder()
        .post(nanaContent1)
        .imageFile(imageFile1)
        .build();
    em.persist(postImageFile1);

    PostImageFile postImageFile2 = PostImageFile.builder()
        .post(nanaContent1)
        .imageFile(imageFile2)
        .build();
    em.persist(postImageFile2);

    PostImageFile postImageFile3 = PostImageFile.builder()
        .post(nanaContent1)
        .imageFile(imageFile3)
        .build();
    em.persist(postImageFile3);

  }

  @Test
  @DisplayName("나나's pick 베너 조회 시 최근에 추가된 4개의 나나가 나온다.")
  void findTop4PreviewDtoOrderByCreatedAt() {
    // Given
    setNana();

    // When
    List<PreviewDto> recentPreviewDtoDto = nanaRepositoryImpl.findTop4PreviewDtoOrderByCreatedAt(
        Language.KOREAN);

    // Then
    Assertions.assertThat(recentPreviewDtoDto.get(0).getId())
        .isEqualTo(nana5.getId());
  }

  @Test
  @DisplayName("나나's pick 전체 리스트 조회 시 최신순으로 보여진다.")
  void findAllPreviewDtoOrderByCreatedAt() {
    // Given
    setNana();

    // When
    Page<PreviewDto> allNanaThumbnailDto = nanaRepositoryImpl.findAllPreviewDtoOrderByCreatedAt(
        Language.KOREAN,
        PageRequest.of(0, 12));
    List<PreviewDto> result = allNanaThumbnailDto.getContent();

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
    Page<PreviewDto> keyword = nanaRepositoryImpl.searchNanaThumbnailDtoByKeyword("keyword",
        Language.KOREAN, PageRequest.of(0, 12));
    List<PreviewDto> content = keyword.getContent();
    for (PreviewDto previewDto : content) {
      System.out.println("nanaThumbnail = " + previewDto.toString());
    }

    // Then
    boolean isSearched = false;
    for (PreviewDto previewDto : content) {
      if (previewDto.getHeading().equals("keyword")) {
        isSearched = true;
      }
    }
    Assertions.assertThat(isSearched).isTrue();
  }

//  @Test
//  void findNanaThumbnailPostDto() {
//    // Given
//    setNana();
//
//    // When
//    NanaThumbnailPost nanaThumbnailPostDto = nanaRepositoryImpl.findNanaThumbnailPostDto(
//        nanaTitle3.getId(), Language.KOREAN);
//    System.out.println("nanaTitle3 = " + nanaTitle3.getId());
//    System.out.println("nanaThumbnailPostDto.toString() = " + nanaThumbnailPostDto.toString());
//
//    // Then
//    Assertions.assertThat(nanaThumbnailPostDto.getId()).isEqualTo(nanaTitle3.getId());
//  }
}