package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    List<MarketThumbnail> data = new ArrayList<>();
    for (MarketCompositeDto marketCompositeDto : marketCompositeDtoPage) {
      data.add(MarketThumbnail.builder()
          .id(marketCompositeDto.getId())
          .title(marketCompositeDto.getTitle())
          .thumbnailUrl(marketCompositeDto.getThumbnailUrl())
          .addressTag(marketCompositeDto.getAddressTag())
          .build());
    }

    return MarketResponse.MarketThumbnailDto.builder()
        .totalElements(marketCompositeDtoPage.getTotalElements())
        .data(data)
        .build();
  }

  public MarketResponse.MarketDetailDto getMarketDetail(MemberInfoDto memberInfoDto, Long id) {
    marketRepository.findById(id)
        .orElseThrow(() -> new BadRequestException("해당 id의 전통시장 게시물이 존재하지 않습니다."));

    Locale locale = memberInfoDto.getLanguage().getLocale();
    MarketCompositeDto resultDto = marketRepository.findCompositeDtoById(id, locale);

    return MarketResponse.MarketDetailDto.builder()
        .id(resultDto.getId())
        .title(resultDto.getTitle())
        .originUrl(resultDto.getOriginUrl())
        .content(resultDto.getContent())
        .address(resultDto.getAddress())
        .addressTag(resultDto.getAddressTag())
        .contact(resultDto.getContact())
        .homepage(resultDto.getHomepage())
        .time(resultDto.getTime())
        .amenity(resultDto.getAmenity())
        .build();
  }

  @Transactional
  public StatusDto toggleLikeStatus(MemberInfoDto memberInfoDto, Long postId) {
    marketRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 전통시장 게시물이 존재하지 않습니다."));

    Boolean status = favoriteService.toggleLikeStatus(memberInfoDto.getMember(),
        CategoryContent.MARKET, postId);

    return FavoriteResponse.StatusDto.builder()
        .isFavorite(status)
        .build();
  }
}
