package com.jeju.nanaland.domain.nana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
public class NanaCardServiceTest {

  @InjectMocks
  NanaCardService nanaCardService;

  @Mock
  NanaRepository nanaRepository;

  @Test
  @DisplayName("나나스픽 카드 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile();
    Nana nana = createNana(imageFile);
    NanaTitle nanaTitle = createNanaTitle(nana);
    PostCardDto postCardDto = PostCardDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(nanaTitle.getHeading())
        .id(nana.getId())
        .category(Category.NANA.toString())
        .build();
    when(nanaRepository.findPostCardDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postCardDto);

    // when
    PostCardDto result = nanaCardService.getPostCardDto(postCardDto.getId(),
        Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postCardDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postCardDto.getTitle());
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Nana createNana(ImageFile imageFile) {
    return Nana.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .version(UUID.randomUUID().toString())
        .build();
  }

  NanaTitle createNanaTitle(Nana nana) {
    return NanaTitle.builder()
        .nana(nana)
        .language(Language.KOREAN)
        .heading(UUID.randomUUID().toString())
        .subHeading(UUID.randomUUID().toString())
        .build();
  }
}
