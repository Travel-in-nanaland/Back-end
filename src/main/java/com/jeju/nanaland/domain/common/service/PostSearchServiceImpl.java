package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.service.ExperienceSearchService;
import com.jeju.nanaland.domain.festival.service.FestivalSearchService;
import com.jeju.nanaland.domain.market.service.MarketSearchService;
import com.jeju.nanaland.domain.nana.service.NanaSearchService;
import com.jeju.nanaland.domain.nature.service.NatureSearchService;
import com.jeju.nanaland.domain.restaurant.service.RestaurantSearchService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PostSearchServiceImpl implements PostSearchService {

  private final NanaSearchService nanaSearchService;
  private final NatureSearchService natureSearchService;
  private final MarketSearchService marketSearchService;
  private final FestivalSearchService festivalSearchService;
  private final ExperienceSearchService experienceSearchService;
  private final RestaurantSearchService restaurantSearchService;

  private final Map<Category, PostSearchService> searchServiceMap = new HashMap<>();

  public PostSearchServiceImpl(NanaSearchService nanaSearchService,
      NatureSearchService natureSearchService, MarketSearchService marketSearchService,
      FestivalSearchService festivalSearchService, ExperienceSearchService experienceSearchService,
      RestaurantSearchService restaurantSearchService) {
    this.nanaSearchService = nanaSearchService;
    this.natureSearchService = natureSearchService;
    this.marketSearchService = marketSearchService;
    this.festivalSearchService = festivalSearchService;
    this.experienceSearchService = experienceSearchService;
    this.restaurantSearchService = restaurantSearchService;

    searchServiceMap.put(Category.NANA, nanaSearchService);
    searchServiceMap.put(Category.NATURE, natureSearchService);
    searchServiceMap.put(Category.MARKET, marketSearchService);
    searchServiceMap.put(Category.FESTIVAL, festivalSearchService);
    searchServiceMap.put(Category.EXPERIENCE, experienceSearchService);
    searchServiceMap.put(Category.RESTAURANT, restaurantSearchService);
  }

  @Override
  public Post getPost(Long postId, Category category) {
    return searchServiceMap.get(category).getPost(postId, category);
  }
}
