package com.jeju.nanaland.domain.restaurant.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantSearchDto;
import com.jeju.nanaland.domain.restaurant.entity.Restaurant;
import com.jeju.nanaland.domain.restaurant.entity.RestaurantTrans;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
public class RestaurantRepositoryTest {

  @Autowired
  RestaurantRepository restaurantRepository;

  @Autowired
  TestEntityManager em;

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("키워드 4개 이하 검색")
  void findSearchDtoByKeywordsUnionTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 12);
    int size = 3;
    for (int i = 0; i < size; i++) {
      Restaurant restaurant = createRestaurant((long) i);
      RestaurantTrans restaurantTrans = createRestaurantTrans(restaurant, i, "test", "제주시", "주소");
      initHashtags(List.of(restaurant), List.of("keyword" + i, "keyword" + (i + 1)), language);
    }

    // when
    Page<RestaurantSearchDto> resultDto = restaurantRepository.findSearchDtoByKeywordsUnion(
        List.of("keyword1", "keyword2"), language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(3);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("키워드 5개 이상 검색")
  void findSearchDtoByKeywordsIntersectTest(Language language) {
    // given
    Pageable pageable = PageRequest.of(0, 12);
    int size = 3;
    for (int i = 0; i < size; i++) {
      Restaurant restaurant = createRestaurant((long) i);
      RestaurantTrans restaurantTrans = createRestaurantTrans(restaurant, i, "test", "제주시", "주소");
      initHashtags(List.of(restaurant),
          List.of("keyword" + i, "keyword" + (i + 1), "keyword" + (i + 2), "keyword" + (i + 3),
              "keyword" + (i + 4)),
          language);
    }

    // when
    Page<RestaurantSearchDto> resultDto = restaurantRepository.findSearchDtoByKeywordsIntersect(
        List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5"), language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(1);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(5);
  }

  @Test
  @DisplayName("맛집 한국어 주소 조회")
  void findKoreanAddressTest() {
    // given
    Restaurant restaurant = createRestaurant(0L);
    createRestaurantTrans(restaurant, 1, "test", "제주시", "주소");

    // when
    Optional<String> koreanAddress = restaurantRepository.findKoreanAddress(restaurant.getId());

    // then
    assertThat(koreanAddress.get()).isEqualTo("주소");
  }

  @Test
  @DisplayName("주소가 null인 경우 한국어 주소 조회")
  void findKoreanAddressFailedTest() {
    // given - 주소가 null
    Restaurant restaurant = createRestaurant(0L);
    createRestaurantTrans(restaurant, 1, "test", "제주시", null);

    // when
    Optional<String> koreanAddress = restaurantRepository.findKoreanAddress(restaurant.getId());

    // then
    assertThat(koreanAddress.isPresent()).isFalse();
  }

  private ImageFile createImageFile(Long number) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin" + number)
        .thumbnailUrl("thumbnail" + number)
        .build();
    em.persist(imageFile);
    return imageFile;
  }

  private Restaurant createRestaurant(Long priority) {
    Restaurant restaurant = Restaurant.builder()
        .firstImageFile(createImageFile(priority))
        .priority(priority)
        .build();
    em.persist(restaurant);
    return restaurant;
  }

  private RestaurantTrans createRestaurantTrans(Restaurant restaurant, int number, String keyword,
      String addressTag, String address) {
    RestaurantTrans restaurantTrans = RestaurantTrans.builder()
        .restaurant(restaurant)
        .language(Language.KOREAN)
        .title(keyword + "title" + number)
        .content("content" + number)
        .addressTag(addressTag)
        .address(address)
        .build();
    em.persist(restaurantTrans);
    return restaurantTrans;
  }

  private void initHashtags(List<Restaurant> restaurants, List<String> keywords,
      Language language) {
    List<Keyword> keywordList = new ArrayList<>();
    for (String k : keywords) {
      TypedQuery<Keyword> query = em.getEntityManager().createQuery(
          "SELECT k FROM Keyword k WHERE k.content = :keyword", Keyword.class);
      query.setParameter("keyword", k);
      List<Keyword> resultList = query.getResultList();

      if (resultList.isEmpty()) {
        Keyword newKeyword = Keyword.builder()
            .content(k)
            .build();
        em.persist(newKeyword);
        keywordList.add(newKeyword);
      } else {
        keywordList.add(resultList.get(0));
      }
    }

    for (Restaurant restaurant : restaurants) {
      for (Keyword k : keywordList) {
        Hashtag newHashtag = Hashtag.builder()
            .post(restaurant)
            .category(Category.RESTAURANT)
            .language(language)
            .keyword(k)
            .build();
        em.persist(newHashtag);
      }
    }
  }
}
