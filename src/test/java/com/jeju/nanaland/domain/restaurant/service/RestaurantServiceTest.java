package com.jeju.nanaland.domain.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.restaurant.entity.Restaurant;
import com.jeju.nanaland.domain.restaurant.entity.RestaurantTrans;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
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
public class RestaurantServiceTest {

  @InjectMocks
  RestaurantService restaurantService;

  @Mock
  RestaurantRepository restaurantRepository;

  @Test
  @DisplayName("맛집 카드 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile();
    Restaurant restaurant = createRestaurant(imageFile);
    RestaurantTrans restaurantTrans = createRestaurantTrans(restaurant);
    PostCardDto postCardDto = PostCardDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(restaurantTrans.getTitle())
        .id(restaurant.getId())
        .category(Category.RESTAURANT.toString())
        .build();
    when(restaurantRepository.findPostCardDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postCardDto);

    // when
    PostCardDto result =
        restaurantService.getPostCardDto(postCardDto.getId(), Category.RESTAURANT, Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postCardDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postCardDto.getTitle());
  }

  @Test
  @DisplayName("맛집 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile();
    Restaurant restaurant = Restaurant.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(restaurantRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(restaurant));

    // when
    Post post = restaurantService.getPost(1L, Category.RESTAURANT);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Restaurant createRestaurant(ImageFile imageFile) {
    return Restaurant.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
  }

  RestaurantTrans createRestaurantTrans(Restaurant restaurant) {
    return RestaurantTrans.builder()
        .restaurant(restaurant)
        .language(Language.KOREAN)
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .build();
  }
}
