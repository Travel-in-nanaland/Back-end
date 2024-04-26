package com.jeju.nanaland.domain.nature.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;

import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureDetailDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
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
public class NatureService {

  private final NatureRepository natureRepository;
  private final FavoriteService favoriteService;


  public NatureThumbnailDto getNatureList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureCompositeDto> natureCompositeDtoPage = natureRepository.findNatureThumbnails(
        memberInfoDto.getLanguage().getLocale(), addressFilterList, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(
        memberInfoDto.getMember(), NATURE);

    List<NatureThumbnail> data = natureCompositeDtoPage.getContent()
        .stream().map(natureCompositeDto ->
            NatureThumbnail.builder()
                .id(natureCompositeDto.getId())
                .title(natureCompositeDto.getTitle())
                .thumbnailUrl(natureCompositeDto.getThumbnailUrl())
                .addressTag(natureCompositeDto.getAddressTag())
                .isFavorite(favoriteIds.contains(natureCompositeDto.getId()))
                .build()).toList();

    return NatureThumbnailDto.builder()
        .totalElements(natureCompositeDtoPage.getTotalElements())
        .data(data)
        .build();
  }

  public NatureDetailDto getNatureDetail(MemberInfoDto memberInfoDto, Long id) {
    NatureCompositeDto natureCompositeDto = natureRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage().getLocale());

    if (natureCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), NATURE,
        id);

    return NatureDetailDto.builder()
        .id(natureCompositeDto.getId())
        .originUrl(natureCompositeDto.getOriginUrl())
        .addressTag(natureCompositeDto.getAddressTag())
        .title(natureCompositeDto.getTitle())
        .content(natureCompositeDto.getContent())
        .intro(natureCompositeDto.getIntro())
        .address(natureCompositeDto.getAddress())
        .contact(natureCompositeDto.getContact())
        .time(natureCompositeDto.getTime())
        .fee(natureCompositeDto.getFee())
        .details(natureCompositeDto.getDetails())
        .amenity(natureCompositeDto.getAmenity())
        .isFavorite(isPostInFavorite)
        .build();
  }
}
