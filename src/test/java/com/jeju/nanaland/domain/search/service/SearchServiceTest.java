package com.jeju.nanaland.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ResultDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SearchServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  SearchService searchService;
  @Autowired
  FavoriteService favoriteService;

  Language language;
  Member member;
  ImageFile imageFile1, imageFile2;
  Category category;
  MemberInfoDto memberInfoDto;

  @BeforeEach
  void init() {
    imageFile1 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile1);
    imageFile2 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile2);

    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language);

    member = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId("123456789")
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile1)
        .build();
    em.persist(member);

    memberInfoDto = MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();

    category = Category.builder()
        .content(CategoryContent.MARKET)
        .build();
    em.persist(category);
  }

  @Test
  void marketSearchTest() {
    /**
     * GIVEN
     *
     * market1 -> title1
     * market2 -> title2
     */
    Market market1 = Market.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(market1);
    Market market2 = Market.builder()
        .imageFile(imageFile2)
        .build();
    em.persist(market2);

    MarketTrans marketTrans1 = MarketTrans.builder()
        .market(market1)
        .language(language)
        .title("title1")
        .build();
    em.persist(marketTrans1);
    MarketTrans marketTrans2 = MarketTrans.builder()
        .market(market2)
        .language(language)
        .title("title2")
        .build();
    em.persist(marketTrans2);

    /**
     * WHEN
     *
     * result1 : keyword = title
     * result2 : keyword = title1
     */
    ResultDto result1 =
        searchService.searchMarketResultDto(memberInfoDto, "title", 0, 4);
    ResultDto result2 =
        searchService.searchMarketResultDto(memberInfoDto, "title1", 0, 4);

    /**
     * THEN
     *
     * result1의 totalElements = 2
     * result2의 totalElements = 1
     */
    assertThat(result1.getData().size()).isEqualTo(2);
    assertThat(result2.getData().size()).isEqualTo(1);
  }
}