package com.jeju.nanaland.domain.nature.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import java.util.Optional;
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
public class NatureSearchServiceTest {

  @InjectMocks
  NatureSearchService natureSearchService;

  @Mock
  NatureRepository natureRepository;

  @Test
  @DisplayName("나나스픽 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile();
    Nature nature = Nature.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(natureRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(nature));

    // when
    Post post = natureSearchService.getPost(1L, Category.NATURE);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

}
