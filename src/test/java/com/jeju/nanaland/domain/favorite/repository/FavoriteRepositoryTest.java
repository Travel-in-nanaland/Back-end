package com.jeju.nanaland.domain.favorite.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
public class FavoriteRepositoryTest {

  @Autowired
  TestEntityManager em;

  @Autowired
  FavoriteRepository favoriteRepository;

  @Test
  @DisplayName("7대자연 찜리스트")
  void findNatureThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = initCategory(CategoryContent.NATURE);
    Pageable pageable = PageRequest.of(0, 12);
    List<Nature> natureList = getNatureList(korean);
    initFavorites(natureList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findNatureThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("nature title 10");
  }

  @Test
  @DisplayName("축제 찜리스트")
  void findFestivalThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = initCategory(CategoryContent.FESTIVAL);
    Pageable pageable = PageRequest.of(0, 12);
    List<Festival> festivalList = getFestivalList(korean);
    initFavorites(festivalList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findFestivalThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("festival title 10");
  }

  @Test
  @DisplayName("전통시장 찜리스트")
  void findMarketThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = initCategory(CategoryContent.MARKET);
    Pageable pageable = PageRequest.of(0, 12);
    List<Market> marketList = getMarketList(korean);
    initFavorites(marketList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findMarketThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("market title 10");
  }

  // TODO: 나나스픽 찜리스트 테스트
//  @Test
//  @DisplayName("나나스픽 찜리스트")
//  void findNanaThumbnailsTest() {
//
//  }

  Language initKoreanLanguage() {
    Language korean = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(korean);
    return korean;
  }

  Category initCategory(CategoryContent categoryContent) {
    Category category = Category.builder()
        .content(categoryContent)
        .build();
    em.persist(category);
    return category;
  }

  Member initMember(Language language) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin url")
        .thumbnailUrl("thumbnail url")
        .build();
    em.persist(imageFile);

    Member member = Member.builder()
        .profileImageFile(imageFile)
        .language(language)
        .provider(Provider.KAKAO)
        .providerId(UUID.randomUUID().toString())
        .email("TEST@naver.com")
        .nickname(UUID.randomUUID().toString())
        .build();
    em.persist(member);

    return member;
  }

  void initFavorites(List<? extends Post> postList, Member member, Category category) {
    for (Post post : postList) {
      Favorite favorite = Favorite.builder()
          .post(post)
          .member(member)
          .category(category)
          .build();
      em.persist(favorite);
    }
  }

  List<Nature> getNatureList(Language language) {
    List<Nature> natureList = new ArrayList<>();

    for (int i = 1; i < 11; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("origin url " + i)
          .thumbnailUrl("thumbnail url " + i)
          .build();
      em.persist(imageFile);

      Nature nature = Nature.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .build();
      em.persist(nature);

      NatureTrans natureTrans = NatureTrans.builder()
          .language(language)
          .nature(nature)
          .title("nature title " + i)
          .build();
      em.persist(natureTrans);

      natureList.add(nature);
    }

    return natureList;
  }

  List<Festival> getFestivalList(Language language) {
    List<Festival> festivalList = new ArrayList<>();

    for (int i = 1; i < 11; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("origin url " + i)
          .thumbnailUrl("thumbnail url " + i)
          .build();
      em.persist(imageFile);

      Festival festival = Festival.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .build();
      em.persist(festival);

      FestivalTrans festivalTrans = FestivalTrans.builder()
          .language(language)
          .festival(festival)
          .title("festival title " + i)
          .build();
      em.persist(festivalTrans);

      festivalList.add(festival);
    }

    return festivalList;
  }

  List<Market> getMarketList(Language language) {
    List<Market> marketList = new ArrayList<>();

    for (int i = 1; i < 11; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("origin url " + i)
          .thumbnailUrl("thumbnail url " + i)
          .build();
      em.persist(imageFile);

      Market market = Market.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .build();
      em.persist(market);

      MarketTrans marketTrans = MarketTrans.builder()
          .language(language)
          .market(market)
          .title("market title " + i)
          .build();
      em.persist(marketTrans);

      marketList.add(market);
    }

    return marketList;
  }
}
