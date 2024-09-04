package com.jeju.nanaland.domain.favorite.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.restaurant.entity.Restaurant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FavoriteRepositoryTest {

  private final Random random = new Random();
  @Autowired
  TestEntityManager em;
  @Autowired
  FavoriteRepository favoriteRepository;
  private Map<Category, Function<Integer, List<? extends Post>>> functionMap;

  void initMap() {
    functionMap = new HashMap<>();
    functionMap.put(Category.NANA, this::createNanas);
    functionMap.put(Category.NATURE, this::createNatures);
    functionMap.put(Category.MARKET, this::createMarkets);
    functionMap.put(Category.FESTIVAL, this::createFestivals);
    functionMap.put(Category.EXPERIENCE, this::createExperiences);
    functionMap.put(Category.RESTAURANT, this::createRestaurants);
  }

  // TODO: Category 에서 NANA_CONTENT 제외
  @DisplayName("유저 전체 찜리스트 조회, 생성 일자 내림차순 조회")
  @Test
  void findAllFavoritesOrderByCreatedAtDescTest() {
    // given
    Member member = createMember(Language.KOREAN);
    int nanaCount = 2;
    int natureCount = 3;
    int marketCount = 4;
    int festivalCount = 5;
    int experienceCount = 6;
    int restaurantCount = 7;
    int experienceInactiveCount = 1;
    int restaurantInactiveCount = 2;
    int totalActiveCount = nanaCount + natureCount + marketCount + festivalCount + experienceCount
        + restaurantCount;
    int total = totalActiveCount + experienceInactiveCount + restaurantInactiveCount;
    Pageable pageable = PageRequest.of(0, total);

    // 게시물 생성
    List<Nana> nanas = createNanas(nanaCount);
    List<Nature> natures = createNatures(natureCount);
    List<Market> markets = createMarkets(marketCount);
    List<Festival> festivals = createFestivals(festivalCount);
    List<Experience> experiences = createExperiences(experienceCount);
    List<Restaurant> restaurants = createRestaurants(restaurantCount);

    // 찜리스트에 등록 - ACTIVE
    List<Favorite> favorites = new ArrayList<>();
    favorites.addAll(createFavorites(member, nanas, Category.NANA, "ACTIVE"));
    favorites.addAll(createFavorites(member, natures, Category.NATURE, "ACTIVE"));
    favorites.addAll(createFavorites(member, markets, Category.MARKET, "ACTIVE"));
    favorites.addAll(createFavorites(member, festivals, Category.FESTIVAL, "ACTIVE"));
    favorites.addAll(createFavorites(member, experiences, Category.EXPERIENCE, "ACTIVE"));
    favorites.addAll(createFavorites(member, restaurants, Category.RESTAURANT, "ACTIVE"));

    // 찜리스트에 등록 - INACTIVE
    List<Experience> inactiveExperiences = createExperiences(experienceInactiveCount);
    List<Restaurant> inactiveRestaurants = createRestaurants(restaurantInactiveCount);
    favorites.addAll(createFavorites(member, inactiveExperiences, Category.EXPERIENCE, "INACTIVE"));
    favorites.addAll(createFavorites(member, inactiveRestaurants, Category.RESTAURANT, "INACTIVE"));

    // when
    Page<Favorite> result = favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(
        member, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(totalActiveCount);
    assertThat(result).extracting("status")
        .allMatch(status -> status.equals("ACTIVE"));
  }

  @DisplayName("유저 카테고리 찜리스트 생성 일자 내림차순 조회")
  @ParameterizedTest
  @EnumSource(value = Category.class, names = "NANA_CONTENT", mode = Mode.EXCLUDE)
  void findAllFavoritesOrderByCreatedAtDescTest(Category category) {
    // given
    Member member = createMember(Language.KOREAN);
    int activeRandomSize = random.nextInt(10);
    int inactiveRandomSize = random.nextInt(10);
    int total = activeRandomSize + inactiveRandomSize;
    Pageable pageable = PageRequest.of(0, total);

    // 카테고리에 해당하는 게시물 생성
    initMap();
    List<? extends Post> activePosts = functionMap.get(category).apply(activeRandomSize);
    List<? extends Post> inactivePosts = functionMap.get(category).apply(inactiveRandomSize);

    // 찜리스트에 등록 - ACTIVE
    List<Favorite> activeFavorites = createFavorites(member, activePosts, category, "ACTIVE");

    // 찜리스트에 등록 - INACTIVE
    List<Favorite> inactiveFavorites = createFavorites(member, inactivePosts, category, "INACTIVE");

    // when
    Page<Favorite> result = favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(
        member, category, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(activeRandomSize);
    assertThat(result).extracting("status")
        .allMatch(status -> status.equals("ACTIVE"));
    assertThat(result).extracting("category")
        .allMatch(resultCategory -> resultCategory.equals(category));
  }

  private Member createMember(Language language) {
    ImageFile profileImage = createImageFile();

    Member member = Member.builder()
        .travelType(TravelType.NONE)
        .profileImageFile(profileImage)
        .language(language)
        .nickname(UUID.randomUUID().toString())
        .email("test@test.com")
        .provider(Provider.KAKAO)
        .providerId("1234567890")
        .build();
    em.persist(member);

    return member;
  }

  private List<Favorite> createFavorites(Member member, List<? extends Post> posts,
      Category category,
      String status) {
    return posts.stream()
        .map(post -> {
          Favorite favorite = Favorite.builder()
              .member(member)
              .post(post)
              .category(category)
              .notificationCount(0)
              .status(status)
              .build();
          em.persist(favorite);
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
      em.persist(newNana);

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
      em.persist(newNature);

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
      em.persist(newExperience);

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
      em.persist(newMarket);

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
      em.persist(newFestival);

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
      em.persist(newRestaurant);

      restaurants.add(newRestaurant);
    }

    return restaurants;
  }

  private ImageFile createImageFile() {
    ImageFile imageFile = ImageFile
        .builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
    em.persist(imageFile);

    return imageFile;
  }
}
