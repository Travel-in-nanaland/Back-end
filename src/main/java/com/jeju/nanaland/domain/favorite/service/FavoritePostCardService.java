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
import org.springframework.stereotype.Service;

@Service
public class FavoritePostCardService {

  private final NatureCardService natureCardService;
  private final NanaCardService nanaCardService;
  private final RestaurantCardService restaurantCardService;
  private final ExperienceCardService experienceCardService;
  private final FestivalCardService festivalCardService;
  private final MarketCardService marketCardService;

  private final Map<Category, PostCardService> postCardMap = new HashMap<>();

  public FavoritePostCardService(NatureCardService natureCardService,
      NanaCardService nanaCardService,
      RestaurantCardService restaurantCardService, ExperienceCardService experienceCardService,
      FestivalCardService festivalCardService, MarketCardService marketCardService) {
    this.natureCardService = natureCardService;
    this.nanaCardService = nanaCardService;
    this.restaurantCardService = restaurantCardService;
    this.experienceCardService = experienceCardService;
    this.festivalCardService = festivalCardService;
    this.marketCardService = marketCardService;

    postCardMap.put(Category.NATURE, natureCardService);
    postCardMap.put(Category.NANA, nanaCardService);
    postCardMap.put(Category.EXPERIENCE, experienceCardService);
    postCardMap.put(Category.RESTAURANT, restaurantCardService);
    postCardMap.put(Category.MARKET, marketCardService);
    postCardMap.put(Category.FESTIVAL, festivalCardService);
  }

  public FavoritePostCardDto getFavoritePostCardDto(Long postId, Language language,
      Category category) {

    // postId, postCategory, originUrl, thumbnailUrl 조회
    PostCardDto postCardDto = postCardMap.get(category).getPostCardDto(postId, language);
    return new FavoritePostCardDto(postCardDto);
  }
}
