package com.jeju.nanaland.domain.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketDetailDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnailDto;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class MarketServiceTest {

  @InjectMocks
  MarketService marketService;

  @Mock
  MarketRepository marketRepository;
  @Mock
  MemberFavoriteService memberFavoriteService;
  @Mock
  ImageFileRepository imageFileRepository;
  @Mock
  private ImageFileService imageFileService;

  @Test
  @DisplayName("전통시장 카드 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile();
    Market market = createMarket(imageFile);
    MarketTrans marketTrans = createMarketTrans(market);
    PostCardDto postCardDto = PostCardDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(marketTrans.getTitle())
        .id(market.getId())
        .category(Category.MARKET.toString())
        .build();
    when(marketRepository.findPostCardDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postCardDto);

    // when
    PostCardDto result =
        marketService.getPostCardDto(postCardDto.getId(), Category.MARKET, Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postCardDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postCardDto.getTitle());
  }

  @Test
  @DisplayName("전통시장 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile();
    Market market = Market.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(marketRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(market));

    // when
    Post post = marketService.getPost(1L, Category.MARKET);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  @Test
  @DisplayName("전통시장 썸네일 페이징")
  void marketThumbnailPagingTest() {
    // given
    Language locale = Language.KOREAN;
    List<String> filterList = Arrays.asList("제주시");
    MemberInfoDto memberInfoDto = createMemberInfoDto(locale, TravelType.NONE);
    Pageable pageable = PageRequest.of(0, 2);

    doReturn(getMarketThumbnailList()).when(marketRepository)
        .findMarketThumbnails(locale, filterList, pageable);
    doReturn(new ArrayList<>()).when(memberFavoriteService)
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
    Language locale = Language.KOREAN;
    MemberInfoDto memberInfoDto = createMemberInfoDto(locale, TravelType.NONE);
    MarketCompositeDto marketDetailDto = MarketCompositeDto.builder()
        .firstImage(new ImageFileDto("first origin url", "first thumbnail url"))
        .build();
    List<ImageFileDto> images = List.of(
        marketDetailDto.getFirstImage(),
        new ImageFileDto("origin url 1", "thumbnail url 1"),
        new ImageFileDto("origin url 2", "thumbnail url 2")
    );

    doReturn(marketDetailDto).when(marketRepository)
        .findCompositeDtoById(any(Long.class), eq(locale));
    doReturn(false).when(memberFavoriteService)
        .isPostInFavorite(any(Member.class), any(Category.class), any(Long.class));
    doReturn(images).when(imageFileService)
        .getPostImageFilesByPostIdIncludeFirstImage(1L, marketDetailDto.getFirstImage());

    // when
    MarketDetailDto result = marketService.getMarketDetail(memberInfoDto, 1L, false);

    // then
    assertThat(result.getImages().size()).isEqualTo(3);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Market createMarket(ImageFile imageFile) {
    return Market.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
  }

  MarketTrans createMarketTrans(Market market) {
    return MarketTrans.builder()
        .market(market)
        .language(Language.KOREAN)
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .build();
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

  private MemberInfoDto createMemberInfoDto(Language language, TravelType travelType) {
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