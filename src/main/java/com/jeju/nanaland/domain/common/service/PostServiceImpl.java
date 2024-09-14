package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.service.ExperienceService;
import com.jeju.nanaland.domain.festival.service.FestivalService;
import com.jeju.nanaland.domain.market.service.MarketService;
import com.jeju.nanaland.domain.nana.service.NanaService;
import com.jeju.nanaland.domain.nature.service.NatureService;
import com.jeju.nanaland.domain.restaurant.service.RestaurantService;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class PostServiceImpl implements PostService {

  private final Map<Category, PostService> serviceMap = new HashMap<>();

  public PostServiceImpl(NanaService nanaService, NatureService natureService,
      MarketService marketService, FestivalService festivalService,
      ExperienceService experienceService, RestaurantService restaurantService) {

    serviceMap.put(Category.NANA, nanaService);
    serviceMap.put(Category.NATURE, natureService);
    serviceMap.put(Category.MARKET, marketService);
    serviceMap.put(Category.FESTIVAL, festivalService);
    serviceMap.put(Category.EXPERIENCE, experienceService);
    serviceMap.put(Category.RESTAURANT, restaurantService);
  }

  /**
   * Post 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException (게시물 id, 카테고리)를 가진 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return serviceMap.get(category).getPost(postId, category);
  }

  /**
   * preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostCardDto
   * @throws NotFoundException (게시물 id, 카테고리, 언어)에 해당하는 게시물 정보가 없는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {
    return serviceMap.get(category).getPostPreviewDto(postId, category, language);
  }
}
