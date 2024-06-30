package com.jeju.nanaland.domain.market.service;

import static com.jeju.nanaland.domain.common.data.Category.MARKET;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnailDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

  private final MarketRepository marketRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;

  public MarketResponse.MarketThumbnailDto getMarketList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList, int page, int size) {

    // default : page = 0, size = 12
    Pageable pageable = PageRequest.of(page, size);
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Page<MarketThumbnail> marketThumbnailPage = marketRepository.findMarketThumbnails(locale,
        addressFilterList, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(
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

  public MarketResponse.MarketDetailDto getMarketDetail(MemberInfoDto memberInfoDto, Long id,
      boolean isSearch) {

    MarketCompositeDto marketCompositeDto = marketRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage().getLocale());

    if (marketCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(MARKET, id);
    }
    // TODO: category 없애는 리팩토링 필요
    // 좋아요 여부 확인
    boolean isFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), MARKET, id);
    // 이미지 리스트
    List<ImageFileDto> images = new ArrayList<>();
    images.add(marketCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(id));

    return MarketResponse.MarketDetailDto.builder()
        .id(marketCompositeDto.getId())
        .images(images)
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
