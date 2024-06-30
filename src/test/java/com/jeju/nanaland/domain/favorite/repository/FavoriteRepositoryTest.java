package com.jeju.nanaland.domain.favorite.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
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
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
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

  final int size = 5;
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
    Category category = Category.NATURE;
    Pageable pageable = PageRequest.of(0, 12);

    // nature 포스트 size개 생성
    List<Nature> natureList = getNatureList(korean, size);
    // favorite에 등록
    initFavorites(natureList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findNatureThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(size);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("nature title " + size);
  }

  @Test
  @DisplayName("postId로 7대자연 썸네일 조회")
  void findNatureThumbnailByPostIdTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = Category.NATURE;

    // nature 포스트 1개 생성
    List<Nature> natureList = getNatureList(korean, 1);
    // favorite에 등록
    initFavorites(natureList, member, category);
    Long postId = natureList.get(0).getId();

    // when
    ThumbnailDto result = favoriteRepository.findNatureThumbnailByPostId(member, postId, locale);

    // then
    assertThat(result).extracting("title").isEqualTo("nature title 1");
  }

  @Test
  @DisplayName("축제 찜리스트")
  void findFestivalThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = Category.FESTIVAL;
    Pageable pageable = PageRequest.of(0, 12);

    // festival 포스트 size개 생성
    List<Festival> festivalList = getFestivalList(korean, size);
    // favorite에 등록
    initFavorites(festivalList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findFestivalThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(size);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("festival title " + size);
  }

  @Test
  @DisplayName("postId로 축제 썸네일 조회")
  void findFestivalThumbnailByPostIdTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = Category.FESTIVAL;

    // festival 포스트 1개 생성
    List<Festival> festivalList = getFestivalList(korean, 1);
    // favorite에 등록
    initFavorites(festivalList, member, category);
    Long postId = festivalList.get(0).getId();

    // when
    ThumbnailDto result = favoriteRepository.findFestivalThumbnailByPostId(member, postId, locale);

    // then
    assertThat(result).extracting("title").isEqualTo("festival title 1");
  }

  @Test
  @DisplayName("전통시장 찜리스트")
  void findMarketThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = Category.MARKET;
    Pageable pageable = PageRequest.of(0, 12);

    // market 포스트 size개 생성
    List<Market> marketList = getMarketList(korean, size);
    // favorite에 등록
    initFavorites(marketList, member, category);

    // when
    Page<ThumbnailDto> result = favoriteRepository.findMarketThumbnails(member, locale, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(size);
    assertThat(result.getContent().get(0))
        .extracting("title").isEqualTo("market title " + size);
  }

  @Test
  @DisplayName("postId로 전통시장 썸네일 조회")
  void findMarketThumbnailByPostIdTest() {
    // given
    Language korean = initKoreanLanguage();
    Member member = initMember(korean);
    Locale locale = Locale.KOREAN;
    Category category = Category.MARKET;

    // market 포스트 1개 생성
    List<Market> marketList = getMarketList(korean, 1);
    // favorite에 등록
    initFavorites(marketList, member, category);
    Long postId = marketList.get(0).getId();

    // when
    ThumbnailDto result = favoriteRepository.findMarketThumbnailByPostId(member, postId, locale);

    // then
    assertThat(result).extracting("title").isEqualTo("market title 1");
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
        .travelType(TravelType.NONE)
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

  List<Nature> getNatureList(Language language, int size) {
    List<Nature> natureList = new ArrayList<>();

    for (int i = 1; i <= size; i++) {
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

  List<Festival> getFestivalList(Language language, int size) {
    List<Festival> festivalList = new ArrayList<>();

    for (int i = 1; i <= size; i++) {
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

  List<Market> getMarketList(Language language, int size) {
    List<Market> marketList = new ArrayList<>();

    for (int i = 1; i <= size; i++) {
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
