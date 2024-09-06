package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.restaurant.entity.Restaurant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class MemberFavoriteServiceTest {

  @InjectMocks
  MemberFavoriteService memberFavoriteService;

  @Mock
  FavoriteRepository favoriteRepository;

  Map<Category, Function<Integer, List<? extends Post>>> createMaps = new HashMap<>();

  Random random = new Random();

  void initCreateFunctionMap() {
    createMaps.put(Category.NANA, this::createNanas);
    createMaps.put(Category.NATURE, this::createNatures);
    createMaps.put(Category.MARKET, this::createMarkets);
    createMaps.put(Category.FESTIVAL, this::createFestivals);
    createMaps.put(Category.EXPERIENCE, this::createExperiences);
    createMaps.put(Category.RESTAURANT, this::createRestaurants);
  }

  @Test
  @DisplayName("회원이 찜한 게시물 id 리스트 조회")
  void getFavoritePostIdsWithMemberTest() {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);
    int randomSize = random.nextInt(10) + 1;
    createExperiences(randomSize);
    List<Experience> experiences = createExperiences(randomSize);
    List<Favorite> favorites = createFavorites(member, experiences, Category.NATURE, "ACTIVE");

    when(favoriteRepository.findAllByMemberAndStatus(member, "ACTIVE"))
        .thenReturn(favorites);

    // when
    List<Long> result = memberFavoriteService.getFavoritePostIdsWithMember(member);

    // then
    assertThat(result.size()).isEqualTo(randomSize);
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  @DisplayName("게시물 찜 상태 확인")
  void isPostInFavoriteTrueTest(Category category) {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);

    initCreateFunctionMap();
    List<? extends Post> posts = createMaps.get(category).apply(1);
    List<Favorite> favorites = createFavorites(member, posts, category, "ACTIVE");
    when(favoriteRepository.findByMemberAndCategoryAndPostIdAndStatus(eq(member), eq(category),
        nullable(Long.class), eq("ACTIVE")))
        .thenReturn(Optional.of(favorites.get(0)));

    // when
    boolean result = memberFavoriteService.isPostInFavorite(member, category, 1L);

    // then
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  @DisplayName("게시물 찜 취소 상태 확인")
  void isPostInFavoriteFalseTest(Category category) {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);

    initCreateFunctionMap();
    List<? extends Post> posts = createMaps.get(category).apply(1);
    List<Favorite> favorites = createFavorites(member, posts, category, "INACTIVE");
    when(favoriteRepository.findByMemberAndCategoryAndPostIdAndStatus(eq(member), eq(category),
        nullable(Long.class), eq("ACTIVE")))
        .thenReturn(Optional.empty());

    // when
    boolean result = memberFavoriteService.isPostInFavorite(member, category, 1L);

    // then
    assertThat(result).isFalse();
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Member createMember(Language language, ImageFile imageFile) {
    return Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(UUID.randomUUID().toString())
        .nickname(UUID.randomUUID().toString())
        .language(language)
        .profileImageFile(imageFile)
        .build();
  }

  private List<Favorite> createFavorites(Member member, List<? extends Post> posts,
      Category category, String status) {
    return posts.stream()
        .map(post -> Favorite.builder()
            .member(member)
            .post(post)
            .category(category)
            .notificationCount(0)
            .status(status)
            .build()
        )
        .collect(Collectors.toList());
  }

  private List<Nana> createNanas(int size) {
    List<Nana> nanas = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();
      Nana newNana = Nana.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .version(String.valueOf(size))
          .build();

      nanas.add(newNana);
    }

    return nanas;
  }

  private List<Nature> createNatures(int size) {
    List<Nature> natures = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();

      Nature newNature = Nature.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .build();

      natures.add(newNature);
    }

    return natures;
  }

  private List<Experience> createExperiences(int size) {
    List<Experience> experiences = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();

      Experience newExperience = Experience.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .build();

      experiences.add(newExperience);
    }

    return experiences;
  }

  private List<Market> createMarkets(int size) {
    List<Market> markets = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();

      Market newMarket = Market.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .build();

      markets.add(newMarket);
    }

    return markets;
  }

  private List<Festival> createFestivals(int size) {
    List<Festival> festivals = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();

      Festival newFestival = Festival.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .build();

      festivals.add(newFestival);
    }

    return festivals;
  }

  private List<Restaurant> createRestaurants(int size) {
    List<Restaurant> restaurants = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ImageFile newImageFile = createImageFile();

      Restaurant newRestaurant = Restaurant.builder()
          .priority(0L)
          .firstImageFile(newImageFile)
          .build();

      restaurants.add(newRestaurant);
    }

    return restaurants;
  }
}
