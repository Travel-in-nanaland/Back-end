package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.experience.service.ExperienceCardService;
import com.jeju.nanaland.domain.favorite.dto.FavoritePostCardDto;
import com.jeju.nanaland.domain.festival.service.FestivalCardService;
import com.jeju.nanaland.domain.market.service.MarketCardService;
import com.jeju.nanaland.domain.nana.service.NanaCardService;
import com.jeju.nanaland.domain.nature.service.NatureCardService;
import com.jeju.nanaland.domain.restaurant.service.RestaurantCardService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoritePostCardService {

  private final NatureCardService natureCardService;
  private final NanaCardService nanaCardService;
  private final RestaurantCardService restaurantCardService;
  private final ExperienceCardService experienceCardService;
  private final FestivalCardService festivalCardService;
  private final MarketCardService marketCardService;

  public FavoritePostCardDto getFavoritePostCardDto(Long postId, Language language,
      Category category) {
    Map<Category, PostCardService> postCardMap = new HashMap<>();
    postCardMap.put(Category.NATURE, natureCardService);
    postCardMap.put(Category.NANA, nanaCardService);
    postCardMap.put(Category.EXPERIENCE, experienceCardService);
    postCardMap.put(Category.RESTAURANT, restaurantCardService);
    postCardMap.put(Category.MARKET, marketCardService);
    postCardMap.put(Category.FESTIVAL, festivalCardService);

    // postId, postCategory, originUrl, thumbnailUrl 조회
    PostCardDto postCardDto = postCardMap.get(category).getPostCardDto(postId, language);
    return new FavoritePostCardDto(postCardDto);
  }
}
