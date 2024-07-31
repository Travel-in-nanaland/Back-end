package com.jeju.nanaland.domain.restaurant.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.Category.RESTAURANT;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantDetailDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnailDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

  private final RestaurantRepository restaurantRepository;
  private final FavoriteService favoriteService;
  private final ReviewRepository reviewRepository;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;

  public RestaurantThumbnailDto getRestaurantList(MemberInfoDto memberInfoDto,
      List<RestaurantTypeKeyword> keywordFilterList, List<String> addressFilterList,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);
    Page<RestaurantThumbnail> restaurantThumbnailPage = restaurantRepository.findRestaurantThumbnails(
        language, keywordFilterList, addressFilterList, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());
    List<RestaurantThumbnail> data = restaurantThumbnailPage.getContent();
    // 좋아요 여부, 리뷰 평균 추가
    for (RestaurantThumbnail restaurantThumbnail : data) {
      Long postId = restaurantThumbnail.getId();
      restaurantThumbnail.setFavorite(favoriteIds.contains(postId));
      restaurantThumbnail.setRatingAvg(reviewRepository.findTotalRatingAvg(RESTAURANT, postId));
    }

    return RestaurantThumbnailDto.builder()
        .totalElements(restaurantThumbnailPage.getTotalElements())
        .data(data)
        .build();
  }

  public RestaurantDetailDto getRestaurantDetails(MemberInfoDto memberInfoDto, Long postId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();
    RestaurantCompositeDto restaurantCompositeDto = restaurantRepository.findCompositeDtoById(
        postId, language);

    // 해당 id의 포스트가 없는 경우 404 에러
    if (restaurantCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(EXPERIENCE, postId);
    }

    // 좋아요 여부 확인
    Member member = memberInfoDto.getMember();
    boolean isFavorite = favoriteService.isPostInFavorite(member, RESTAURANT, postId);

    // 이미지
    List<ImageFileDto> images = new ArrayList<>();
    images.add(restaurantCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(postId));

    // 키워드
    Set<RestaurantTypeKeyword> keywordSet = restaurantRepository.getRestaurantTypeKeywordSet(
        postId);
    List<String> keywords = keywordSet.stream()
        .map(experienceTypeKeyword ->
            experienceTypeKeyword.getValueByLocale(language)
        ).toList();

    // 메뉴
    List<RestaurantMenuDto> menuDtoList = restaurantRepository.getRestaurantMenuList(postId,
        language);

    return RestaurantDetailDto.builder()
        .id(restaurantCompositeDto.getId())
        .title(restaurantCompositeDto.getTitle())
        .content(restaurantCompositeDto.getContent())
        .address(restaurantCompositeDto.getAddress())
        .addressTag(restaurantCompositeDto.getAddressTag())
        .contact(restaurantCompositeDto.getContact())
        .homepage(restaurantCompositeDto.getHomepage())
        .instagram(restaurantCompositeDto.getInstagram())
        .time(restaurantCompositeDto.getTime())
        .service(restaurantCompositeDto.getService())
        .keywords(keywords)
        .menus(menuDtoList)
        .isFavorite(isFavorite)
        .images(images)
        .build();
  }
}
