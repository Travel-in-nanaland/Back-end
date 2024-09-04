package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.experience.service.ExperienceCardService;
import com.jeju.nanaland.domain.favorite.dto.FavoritePostCardDto;
import com.jeju.nanaland.domain.festival.service.FestivalCardService;
import com.jeju.nanaland.domain.market.service.MarketCardService;
import com.jeju.nanaland.domain.nana.service.NanaCardService;
import com.jeju.nanaland.domain.nature.service.NatureCardService;
import com.jeju.nanaland.domain.restaurant.service.RestaurantCardService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
public class FavoriteCardServiceTest {

  @InjectMocks
  FavoritePostCardService favoritePostCardService;

  @Mock
  NanaCardService nanaCardService;
  @Mock
  NatureCardService natureCardService;
  @Mock
  FestivalCardService festivalCardService;
  @Mock
  MarketCardService marketCardService;
  @Mock
  ExperienceCardService experienceCardService;
  @Mock
  RestaurantCardService restaurantCardService;

  Map<Category, PostCardService> postCardServiceMap = new HashMap<>();

  @BeforeEach
  void initPostCardServiceMap() {
    postCardServiceMap.put(Category.NANA, nanaCardService);
    postCardServiceMap.put(Category.NATURE, natureCardService);
    postCardServiceMap.put(Category.MARKET, marketCardService);
    postCardServiceMap.put(Category.FESTIVAL, festivalCardService);
    postCardServiceMap.put(Category.EXPERIENCE, experienceCardService);
    postCardServiceMap.put(Category.RESTAURANT, restaurantCardService);
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  @DisplayName("찜 게시물 카드조회")
  void getFavoritePostCardDtoTest(Category category) {
    // given
    ImageFile imageFile = createImageFile();
    PostCardDto postCardDto = PostCardDto.builder()
        .id(1L)
        .title(UUID.randomUUID().toString())
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .category(category.toString())
        .build();
    FavoritePostCardDto favoritePostCardDto = new FavoritePostCardDto(postCardDto);

    when(postCardServiceMap.get(category).getPostCardDto(1L, Language.KOREAN))
        .thenReturn(favoritePostCardDto);

    // when
    FavoritePostCardDto result = favoritePostCardService.getFavoritePostCardDto(1L,
        Language.KOREAN, category);

    // then
    assertThat(result.getCategory()).isEqualTo(category.toString());
    assertThat(result.getTitle()).isEqualTo(postCardDto.getTitle());
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }
}
