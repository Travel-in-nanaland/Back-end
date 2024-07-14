package com.jeju.nanaland.domain.restaurant.service;

import static com.jeju.nanaland.domain.common.data.Category.RESTAURANT;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnailDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
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
public class RestaurantService {

  private final RestaurantRepository restaurantRepository;
  private final FavoriteService favoriteService;
  private final ReviewRepository reviewRepository;

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
}
