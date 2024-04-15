package com.jeju.nanaland.domain.entity.nana;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class NanaRepositoryTest {

  @PersistenceContext
  EntityManager em;

  @Autowired
  NanaRepository nanaRepository;

  Language language;

  Language language2;

  ImageFile imageFile;
  ImageFile imageFile2;

  @BeforeEach
  void init() {
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    language2 = Language.builder()
        .locale(Locale.CHINESE)
        .dateFormat("yyyy-mm-dd")
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
  }

  @Test
  @DisplayName("Nana init")
  void nanaQueryDslTest() {
    Nana nana1 = Nana.builder()
        .version("ver1")
        .build();
    em.persist(nana1);

    Nana nana2 = Nana.builder()

        .version("ver1")
        .build();
    em.persist(nana2);

    Nana nana3 = Nana.builder()
        .version("ver1")
        .build();
    em.persist(nana3);

    //nana1=> active = true / language2=> chinese
    NanaTitle nanaTitle1 = NanaTitle.builder()
        .notice("notice1")
        .imageFile(imageFile)
        .language(language2)
        .nana(nana1)
        .build();
    em.persist(nanaTitle1);

    //nana3=> active = true / language2=> chinese
    NanaTitle nanaTitle2 = NanaTitle.builder()
        .notice("notice2")
        .imageFile(imageFile2)
        .language(language2)
        .nana(nana3)
        .build();
    em.persist(nanaTitle2);

    NanaContent nanaContent1 = NanaContent.builder()
        .subTitle("subtitle1")
        .nanaTitle(nanaTitle1)
        .content("content")
        .number(1)
        .title("title")
        .imageFile(imageFile)
        .build();
    em.persist(nanaContent1);

    NanaContent nanaContent2 = NanaContent.builder()
        .subTitle("subtitle2")
        .nanaTitle(nanaTitle1)
        .content("content2")
        .number(2)
        .title("title2")
        .imageFile(imageFile)
        .build();
    em.persist(nanaContent2);

    //Locale = chinese / active = true인 Nana 찾기
    List<NanaResponse.NanaThumbnail> thumbnailDto = nanaRepository.findRecentNanaThumbnailDto(
        Locale.CHINESE);
    Assertions.assertThat(thumbnailDto.size()).isEqualTo(2);

  }
}
