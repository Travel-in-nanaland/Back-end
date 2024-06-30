package com.jeju.nanaland.domain.nature.service;

import static com.jeju.nanaland.domain.common.data.Category.NATURE;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureDetailDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
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
public class NatureService {

  private final NatureRepository natureRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;


  public NatureThumbnailDto getNatureList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList, String keyword, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureThumbnail> natureCompositeDtoPage = natureRepository.findNatureThumbnails(
        memberInfoDto.getLanguage().getLocale(), addressFilterList, keyword, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());

    List<NatureThumbnail> data = natureCompositeDtoPage.getContent();
    for (NatureThumbnail natureThumbnail : data) {
      natureThumbnail.setFavorite(favoriteIds.contains(natureThumbnail.getId()));
    }

    return NatureThumbnailDto.builder()
        .totalElements(natureCompositeDtoPage.getTotalElements())
        .data(data)
        .build();
  }

  public NatureDetailDto getNatureDetail(MemberInfoDto memberInfoDto, Long id, boolean isSearch) {
    NatureCompositeDto natureCompositeDto = natureRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage().getLocale());

    if (natureCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    if (isSearch) {
      searchService.updateSearchVolumeV1(NATURE, id);
    }

    // TODO: category 없애는 리팩토링 필요
    boolean isFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), NATURE,
        id);

    // 이미지 리스트
    List<ImageFileDto> images = new ArrayList<>();
    images.add(natureCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(id));

    return NatureDetailDto.builder()
        .id(natureCompositeDto.getId())
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
        .isFavorite(isFavorite)
        .images(images)
        .build();
  }
}
