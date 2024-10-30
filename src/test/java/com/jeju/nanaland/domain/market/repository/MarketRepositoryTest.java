package com.jeju.nanaland.domain.market.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MarketRepositoryTest {

  @Autowired
  MarketRepository marketRepository;

  @Autowired
  TestEntityManager em;

  @Test
  @DisplayName("전통시장 검색")
  void searchMarketTest() {
    Pageable pageable = PageRequest.of(0, 12);
    marketRepository.searchCompositeDtoByKeyword("쇼핑", Language.KOREAN, pageable);
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
}