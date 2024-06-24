package com.jeju.nanaland.domain.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketDetailDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnailDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

  @InjectMocks
  MarketService marketService;

  @Mock
  MarketRepository marketRepository;
  @Mock
  FavoriteService favoriteService;
  @Mock
  ImageFileRepository imageFileRepository;

  @Test
  @DisplayName("전통시장 썸네일 페이징")
  void marketThumbnailPagingTest() {
    // given
    Locale locale = Locale.KOREAN;
    List<String> filterList = Arrays.asList("제주시");
    MemberInfoDto memberInfoDto = createMemberInfoDto(locale, TravelType.NONE);
    Pageable pageable = PageRequest.of(0, 2);

    doReturn(getMarketThumbnailList()).when(marketRepository)
        .findMarketThumbnails(locale, filterList, pageable);
    doReturn(new ArrayList<>()).when(favoriteService)
        .getFavoritePostIdsWithMember(any(Member.class));

    // when
    MarketThumbnailDto result = marketService.getMarketList(memberInfoDto, filterList, 0, 2);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getData().size()).isEqualTo(2);
    assertThat(result.getData().get(0).getTitle()).isEqualTo("market title 1");
  }

  @Test
  @DisplayName("전통시장 상세조회")
  void marketDetailTest() {
    // given
    Locale locale = Locale.KOREAN;
    MemberInfoDto memberInfoDto = createMemberInfoDto(locale, TravelType.NONE);
    MarketCompositeDto marketDetailDto = MarketCompositeDto.builder()
        .firstImage(new ImageFileDto("first origin url", "first thumbnail url"))
        .build();
    List<ImageFileDto> additionalImages = Arrays.asList(
        new ImageFileDto("origin url 1", "thumbnail url 1"),
        new ImageFileDto("origin url 2", "thumbnail url 2"));

    doReturn(marketDetailDto).when(marketRepository)
        .findCompositeDtoById(any(Long.class), eq(locale));
    doReturn(false).when(favoriteService)
        .isPostInFavorite(any(Member.class), any(CategoryContent.class), any(Long.class));
    doReturn(additionalImages).when(imageFileRepository)
        .findPostImageFiles(any(Long.class));

    // when
    MarketDetailDto result = marketService.getMarketDetail(memberInfoDto, 1L, false);

    // then
    assertThat(result.getFirstImage().getOriginUrl()).isEqualTo("first origin url");
    assertThat(result.getImages().size()).isEqualTo(2);
  }

  // totalElement: 10, MarketThumbnail 데이터가 2개인 Page 생성
  private Page<MarketThumbnail> getMarketThumbnailList() {
    List<MarketThumbnail> marketThumbnailList = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      marketThumbnailList.add(
          MarketThumbnail.builder()
              .title("market title " + i)
              .addressTag("제주시")
              .build());
    }

    return new PageImpl<>(marketThumbnailList, PageRequest.of(0, 2), 10);
  }

  private MemberInfoDto createMemberInfoDto(Locale locale, TravelType travelType) {
    Language language = Language.builder()
        .locale(locale)
        .build();
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }
}