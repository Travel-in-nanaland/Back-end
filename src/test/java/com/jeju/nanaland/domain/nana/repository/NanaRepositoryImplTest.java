package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
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
  ImageFile imageFile, imageFile2, imageFile3, imageFile4, imageFile5;
  Nana nana1, nana2, nana3, nana4, nana5;
  NanaTitle nanaTitle1, nanaTitle2, nanaTitle3, nanaTitle4, nanaTitle5;
  NanaContent nanaContent1, nanaContent2, nanaContent3;
  @PersistenceContext
  private EntityManager em;
  @Autowired
  private NanaRepositoryImpl nanarepositoryImpl;
  @Autowired
  private NanaRepository nanaRepository;
  @Autowired
  private NanaTitleRepository nanaTitleRepository;
  @Autowired
  private NanaContentRepository nanaContentRepository;

  // nana 5개, 각 nana에 nanaTitle 1개(nana 1개당 nanaTitle 1개씩 -korean만),nanaTitle1에 nanaContent 3개
  private void setNana() {
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language);

    language2 = Language.builder()
        .locale(Locale.CHINESE)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language2);

    imageFile = ImageFile.builder()
        .originUrl("originUrl")
        .thumbnailUrl("thumbnailUrl")
        .build();
    em.persist(imageFile);

    imageFile2 = ImageFile.builder()
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
        .build();
    em.persist(imageFile2);

    imageFile3 = ImageFile.builder()
        .originUrl("originUrl2")
        .thumbnailUrl("thumbnailUrl2")
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
        .build();
    nanaRepository.save(nana1);

    nana2 = Nana.builder()
        .version("ver2")
        .build();
    nanaRepository.save(nana2);

    nana3 = Nana.builder()
        .version("ver3")
        .build();
    nanaRepository.save(nana3);

    nana4 = Nana.builder()
        .version("ver4")
        .build();
    nanaRepository.save(nana4);

    nana5 = Nana.builder()
        .version("ver5")
        .build();
    nanaRepository.save(nana5);

    nanaTitle1 = NanaTitle.builder()
        .notice("notice1")
        .imageFile(imageFile)
        .language(language)
        .nana(nana1)
        .build();
    nanaTitleRepository.save(nanaTitle1);

    //nana3=> active = true / language2=> chinese
    nanaTitle2 = NanaTitle.builder()
        .notice("notice2")
        .heading("keyword")
        .imageFile(imageFile2)
        .language(language)
        .nana(nana2)
        .build();
    nanaTitleRepository.save(nanaTitle2);

    nanaTitle3 = NanaTitle.builder()
        .notice("notice3")
        .imageFile(imageFile3)
        .language(language)
        .nana(nana3)
        .build();
    nanaTitleRepository.save(nanaTitle3);

    nanaTitle4 = NanaTitle.builder()
        .notice("notice4")
        .imageFile(imageFile4)
        .language(language)
        .nana(nana4)
        .build();
    nanaTitleRepository.save(nanaTitle4);
    nanaTitle5 = NanaTitle.builder()
        .notice("notice5")
        .imageFile(imageFile5)
        .language(language)
        .nana(nana5)
        .build();
    nanaTitleRepository.save(nanaTitle5);

    nanaContent1 = NanaContent.builder()
        .subTitle("subtitle1")
        .nanaTitle(nanaTitle1)
        .content("content")
        .number(1)
        .title("title")
        .imageFile(imageFile)
        .build();
    nanaContentRepository.save(nanaContent1);

    nanaContent2 = NanaContent.builder()
        .subTitle("subtitle2")
        .nanaTitle(nanaTitle1)
        .content("content2")
        .number(2)
        .title("title2")
        .imageFile(imageFile2)
        .build();
    nanaContentRepository.save(nanaContent2);

    nanaContent3 = NanaContent.builder()
        .subTitle("subtitle3")
        .nanaTitle(nanaTitle1)
        .content("content3")
        .number(2)
        .title("title3")
        .imageFile(imageFile3)
        .build();
    nanaContentRepository.save(nanaContent3);

  }

  @Test
  @DisplayName("나나's pick 베너 조회 시 최근에 추가된 4개의 나나가 나온다.")
  void findRecentNanaThumbnailDto() {
    // Given
    setNana();

    // When
    List<NanaThumbnail> recentNanaThumbnailDto = nanarepositoryImpl.findRecentNanaThumbnailDto(
        Locale.KOREAN);

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
        Locale.KOREAN,
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
        Locale.KOREAN, PageRequest.of(0, 12));
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
        nanaTitle3.getId(), Locale.KOREAN);

    // Then
    Assertions.assertThat(nanaThumbnailPostDto.getId()).isEqualTo(nanaTitle3.getId());
  }
}