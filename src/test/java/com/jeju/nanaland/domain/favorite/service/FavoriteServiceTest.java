package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest.LikeToggleDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.FavoriteCardPageDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class FavoriteServiceTest {

  @InjectMocks
  FavoriteService favoriteService;

  @Mock
  FavoriteRepository favoriteRepository;
  @Mock
  PostService postService;

  Random random = new Random();

  Map<Category, Function<Integer, List<? extends Post>>> createMaps = new HashMap<>();

  Map<Category, Post> postMap = new HashMap<>();

  void initCreateFunctionMap() {
    createMaps.put(Category.NANA, this::createNanas);
    createMaps.put(Category.NATURE, this::createNatures);
    createMaps.put(Category.MARKET, this::createMarkets);
    createMaps.put(Category.FESTIVAL, this::createFestivals);
    createMaps.put(Category.EXPERIENCE, this::createExperiences);
    createMaps.put(Category.RESTAURANT, this::createRestaurants);
  }

  void initPostMap() {
    postMap.put(Category.NANA, Nana.builder().build());
    postMap.put(Category.NATURE, Nature.builder().build());
    postMap.put(Category.MARKET, Market.builder().build());
    postMap.put(Category.FESTIVAL, Festival.builder().build());
    postMap.put(Category.EXPERIENCE, Experience.builder().build());
    postMap.put(Category.RESTAURANT, Restaurant.builder().build());
  }

  @Test
  @DisplayName("찜리스트 전체 조회")
  void getAllFavoritesTest() {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member);
    int nanaSize = 1;
    int natureSize = 2;
    int marketSize = 3;
    int festivalSize = 4;
    int experienceSize = 5;
    int restaurantSize = 6;
    int total = nanaSize + natureSize + marketSize + festivalSize + experienceSize + restaurantSize;
    Pageable pageable = PageRequest.of(0, total);

    // 게시물 생성
    List<Nana> nanas = createNanas(nanaSize);
    List<Nature> natures = createNatures(natureSize);
    List<Market> markets = createMarkets(marketSize);
    List<Festival> festivals = createFestivals(festivalSize);
    List<Experience> experiences = createExperiences(experienceSize);
    List<Restaurant> restaurants = createRestaurants(restaurantSize);

    // 찜 등록
    List<Favorite> favorites = new ArrayList<>();
    favorites.addAll(createFavorites(member, nanas, Category.NANA, "ACTIVE"));
    favorites.addAll(createFavorites(member, natures, Category.NATURE, "ACTIVE"));
    favorites.addAll(createFavorites(member, markets, Category.MARKET, "ACTIVE"));
    favorites.addAll(createFavorites(member, festivals, Category.FESTIVAL, "ACTIVE"));
    favorites.addAll(createFavorites(member, experiences, Category.EXPERIENCE, "ACTIVE"));
    favorites.addAll(createFavorites(member, restaurants, Category.RESTAURANT, "ACTIVE"));

    when(favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(member, pageable))
        .thenReturn(new PageImpl<>(favorites, pageable, favorites.size()));
    when(postService.getPostCardDto(nullable(Long.class), any(Category.class), eq(Language.KOREAN)))
        .thenReturn(PostCardDto.builder().build());

    // when
    FavoriteCardPageDto result = favoriteService.getAllFavorites(memberInfoDto, 0, total);

    // then
    assertThat(result.getTotalElements()).isEqualTo(total);
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  @DisplayName("카테고리별 찜리스트 조회")
  void getAllCategoryFavoritesTest(Category category) {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member);
    int randomSize = random.nextInt(10) + 1;
    Pageable pageable = PageRequest.of(0, randomSize);

    // 게시물 생성
    initCreateFunctionMap();
    List<? extends Post> posts = createMaps.get(category).apply(randomSize);

    // 찜 등록
    List<Favorite> favorites = createFavorites(member, posts, category, "ACTIVE");

    when(favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(member, category, pageable))
        .thenReturn(new PageImpl<>(favorites, pageable, favorites.size()));
    when(postService.getPostCardDto(nullable(Long.class), any(Category.class), eq(Language.KOREAN)))
        .thenReturn(PostCardDto.builder().build());

    // when
    FavoriteCardPageDto result = favoriteService.getAllCategoryFavorites(memberInfoDto, category,
        0, randomSize);

    // then
    assertThat(result.getTotalElements()).isEqualTo(randomSize);
  }

  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  @DisplayName("좋아요 토글 테스트 - 처음 좋아요를 누를 때")
  void toggleLikeStatusTest(Category category) {
    // given
    ImageFile imageFile = createImageFile();
    Member member = createMember(Language.KOREAN, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member);
    Long postId = 1L;

    initPostMap();
    Post post = postMap.get(category);
    when(favoriteRepository.findByMemberAndCategoryAndPostId(member, category, postId))
        .thenReturn(Optional.empty());
    when(postService.getPost(postId, category))
        .thenReturn(post);
    when(favoriteRepository.save(any(Favorite.class)))
        .thenReturn(Favorite.builder().build());

    LikeToggleDto likeToggleDto = LikeToggleDto.builder()
        .id(1L)
        .category(category.toString())
        .build();

    // when
    StatusDto result = favoriteService.toggleLikeStatus(memberInfoDto, likeToggleDto);

    // then
    assertThat(result.isFavorite()).isTrue();
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

  MemberInfoDto createMemberInfoDto(Member member) {
    return MemberInfoDto.builder()
        .member(member)
        .language(member.getLanguage())
        .build();
  }

  private List<Favorite> createFavorites(Member member, List<? extends Post> posts,
      Category category, String status) {
    return posts.stream()
        .map(post -> {
          Favorite favorite = Favorite.builder()
              .member(member)
              .post(post)
              .category(category)
              .notificationCount(0)
              .status(status)
              .build();
          return favorite;
        })
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