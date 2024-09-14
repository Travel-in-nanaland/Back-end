package com.jeju.nanaland.domain.market.service;

import static com.jeju.nanaland.domain.common.data.Category.MARKET;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnailDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService implements PostService {

  private final MarketRepository marketRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final SearchService searchService;
  private final ImageFileService imageFileService;

  /**
   * Market 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException 게시물 id에 해당하는 전통시장 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return marketRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }

  /**
   * 게시물 preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostPreviewDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 전통시장 정보가 존재하지 않는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {
    PostPreviewDto postPreviewDto = marketRepository.findPostPreviewDto(postId, language);
    Optional.ofNullable(postPreviewDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postPreviewDto.setCategory(PostCategory.MARKET.toString());
    return postPreviewDto;
  }

  // 전통시장 리스트 조회
  public MarketResponse.MarketThumbnailDto getMarketList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Language locale = memberInfoDto.getLanguage();
    Page<MarketThumbnail> marketThumbnailPage = marketRepository.findMarketThumbnails(locale,
        addressFilterList, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());
    List<MarketThumbnail> data = marketThumbnailPage.getContent();

    // favorite에 해당 id가 존재하면 isFavorite 필드 true, 아니라면 false
    for (MarketThumbnail marketThumbnail : data) {
      marketThumbnail.setFavorite(favoriteIds.contains(marketThumbnail.getId()));
    }

    return MarketThumbnailDto.builder()
        .totalElements(marketThumbnailPage.getTotalElements())
        .data(data)
        .build();
  }

  // 전통시장 상세 정보 조회
  public MarketResponse.MarketDetailDto getMarketDetail(MemberInfoDto memberInfoDto, Long id,
      boolean isSearch) {

    MarketCompositeDto marketCompositeDto = marketRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage());

    if (marketCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(MARKET, id);
    }

    // 좋아요 여부 확인
    boolean isFavorite =
        memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), MARKET, id);

    return MarketResponse.MarketDetailDto.builder()
        .id(marketCompositeDto.getId())
        .images(imageFileService.getPostImageFilesByPostIdIncludeFirstImage(id,
            marketCompositeDto.getFirstImage()))
        .title(marketCompositeDto.getTitle())
        .content(marketCompositeDto.getContent())
        .address(marketCompositeDto.getAddress())
        .addressTag(marketCompositeDto.getAddressTag())
        .contact(marketCompositeDto.getContact())
        .homepage(marketCompositeDto.getHomepage())
        .time(marketCompositeDto.getTime())
        .amenity(marketCompositeDto.getAmenity())
        .isFavorite(isFavorite)
        .build();
  }
}
