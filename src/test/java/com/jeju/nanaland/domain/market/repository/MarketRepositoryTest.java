package com.jeju.nanaland.domain.market.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketSearchDto;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MarketRepositoryTest {

  @Autowired
  MarketRepository marketRepository;

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
      Market market = createMarket((long) i);
      MarketTrans marketTrans = createMarketTrans(market, i, "test", "제주시");
      initHashtags(List.of(market), List.of("keyword" + i, "keyword" + (i + 1)), language);
    }

    // when
    Page<MarketSearchDto> resultDto = marketRepository.findSearchDtoByKeywordsUnion(
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
      Market market = createMarket((long) i);
      MarketTrans marketTrans = createMarketTrans(market, i, "test", "제주시");
      initHashtags(List.of(market),
          List.of("keyword" + i, "keyword" + (i + 1), "keyword" + (i + 2), "keyword" + (i + 3),
              "keyword" + (i + 4)),
          language);
    }

    // when
    Page<MarketSearchDto> resultDto = marketRepository.findSearchDtoByKeywordsIntersect(
        List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5"), language, pageable);

    // then
    assertThat(resultDto.getTotalElements()).isEqualTo(1);
    assertThat(resultDto.getContent().get(0).getMatchedCount()).isEqualTo(5);
  }

  @Test
  @DisplayName("전통시장 썸네일 조회")
  void findMarketThumbnailsTest() {
    // given
    Language korean = initKoreanLanguage();
    List<Market> marketList = getMarketList(korean);
    for (Market market : marketList) {
      System.out.println(market.getCreatedAt());
    }

    Language locale = Language.KOREAN;
    List<AddressTag> addressFilter = Arrays.asList(AddressTag.JEJU);
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<MarketThumbnail> marketThumbnails = marketRepository.findMarketThumbnails(locale,
        addressFilter, pageable);
    List<MarketThumbnail> thumbnails = marketThumbnails.getContent();

    // then
    assertThat(thumbnails).hasSize(2);
    assertThat(thumbnails.get(0)).extracting("title").isEqualTo("market title 10");
    assertThat(thumbnails.get(1)).extracting("title").isEqualTo("market title 9");
  }

  // KOREAN 언어 정보 초기 설정
  Language initKoreanLanguage() {
    Language korean = Language.KOREAN;
    return korean;
  }

  // 언어: KOREAN, 전통시장 데이터 10개 생성
  List<Market> getMarketList(Language language) {
    List<Market> marketList = new ArrayList<>();
    for (int i = 1; i < 11; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("originUrl" + i)
          .thumbnailUrl("thumbnailUrl" + i)
          .build();
      em.persist(imageFile);
      Market market = Market.builder()
          .firstImageFile(imageFile)
          .priority((long) i)
          .build();
      em.persistAndFlush(market);
      MarketTrans marketTrans1 = MarketTrans.builder()
          .market(market)
          .title("market title " + i)
          .language(language)
          .addressTag("제주시")
          .build();
      em.persist(marketTrans1);

      marketList.add(market);
    }

    return marketList;
  }

  private ImageFile createImageFile(Long number) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin" + number)
        .thumbnailUrl("thumbnail" + number)
        .build();
    em.persist(imageFile);
    return imageFile;
  }

  private Market createMarket(Long priority) {
    Market market = Market.builder()
        .firstImageFile(createImageFile(priority))
        .priority(priority)
        .build();
    em.persist(market);
    return market;
  }

  private MarketTrans createMarketTrans(Market market, int number, String keyword,
      String addressTag) {
    MarketTrans marketTrans = MarketTrans.builder()
        .market(market)
        .language(Language.KOREAN)
        .title(keyword + "title" + number)
        .content("content" + number)
        .addressTag(addressTag)
        .build();
    em.persist(marketTrans);
    return marketTrans;
  }

  private void initHashtags(List<Market> markets, List<String> keywords,
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

    for (Market market : markets) {
      for (Keyword k : keywordList) {
        Hashtag newHashtag = Hashtag.builder()
            .post(market)
            .category(Category.MARKET)
            .language(language)
            .keyword(k)
            .build();
        em.persist(newHashtag);
      }
    }
  }
}