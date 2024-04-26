package com.jeju.nanaland.domain.market.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnailDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
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

  public MarketResponse.MarketThumbnailDto getMarketList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList,
      int page, int size) {

    // default : page = 0, size = 12
    Pageable pageable = PageRequest.of(page, size);
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Page<MarketCompositeDto> marketCompositeDtoPage = marketRepository.findMarketThumbnails(locale,
        addressFilterList, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(
        memberInfoDto.getMember(), MARKET);

    List<MarketThumbnail> data = marketCompositeDtoPage.getContent()
        .stream().map(marketCompositeDto ->
            MarketThumbnail.builder()
                .id(marketCompositeDto.getId())
                .title(marketCompositeDto.getTitle())
                .thumbnailUrl(marketCompositeDto.getThumbnailUrl())
                .addressTag(marketCompositeDto.getAddressTag())
                .isFavorite(favoriteIds.contains(marketCompositeDto.getId()))
                .build()).toList();

    return MarketThumbnailDto.builder()
        .totalElements(marketCompositeDtoPage.getTotalElements())
        .data(data)
        .build();
  }

  public MarketResponse.MarketDetailDto getMarketDetail(MemberInfoDto memberInfoDto, Long id) {

    MarketCompositeDto marketCompositeDto = marketRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage().getLocale());

    if (marketCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), MARKET,
        id);

    return MarketResponse.MarketDetailDto.builder()
        .id(marketCompositeDto.getId())
        .title(marketCompositeDto.getTitle())
        .originUrl(marketCompositeDto.getOriginUrl())
        .content(marketCompositeDto.getContent())
        .address(marketCompositeDto.getAddress())
        .addressTag(marketCompositeDto.getAddressTag())
        .contact(marketCompositeDto.getContact())
        .homepage(marketCompositeDto.getHomepage())
        .time(marketCompositeDto.getTime())
        .amenity(marketCompositeDto.getAmenity())
        .isFavorite(isPostInFavorite)
        .build();
  }
}
