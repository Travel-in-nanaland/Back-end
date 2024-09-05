package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.service.ExperienceService;
import com.jeju.nanaland.domain.festival.service.FestivalService;
import com.jeju.nanaland.domain.market.service.MarketService;
import com.jeju.nanaland.domain.nana.service.NanaService;
import com.jeju.nanaland.domain.nature.service.NatureService;
import com.jeju.nanaland.domain.restaurant.service.RestaurantService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class PostServiceImpl implements PostService {

  private final NanaService nanaService;
  private final NatureService natureService;
  private final MarketService marketService;
  private final FestivalService festivalService;
  private final ExperienceService experienceService;
  private final RestaurantService restaurantService;

  private final Map<Category, PostService> serviceMap = new HashMap<>();

  public PostServiceImpl(NanaService nanaService, NatureService natureService,
      MarketService marketService, FestivalService festivalService,
      ExperienceService experienceService, RestaurantService restaurantService) {
    this.nanaService = nanaService;
    this.natureService = natureService;
    this.marketService = marketService;
    this.festivalService = festivalService;
    this.experienceService = experienceService;
    this.restaurantService = restaurantService;

    serviceMap.put(Category.NANA, nanaService);
    serviceMap.put(Category.NATURE, natureService);
    serviceMap.put(Category.MARKET, marketService);
    serviceMap.put(Category.FESTIVAL, festivalService);
    serviceMap.put(Category.EXPERIENCE, experienceService);
    serviceMap.put(Category.RESTAURANT, restaurantService);
  }

  @Override
  public Post getPost(Long postId, Category category) {
    return serviceMap.get(category).getPost(postId, category);
  }

  @Override
  public PostCardDto getPostCardDto(Long postId, Category category, Language language) {
    return serviceMap.get(category).getPostCardDto(postId, category, language);
  }
}
