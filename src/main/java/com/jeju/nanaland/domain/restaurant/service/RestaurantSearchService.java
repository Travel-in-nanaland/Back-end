package com.jeju.nanaland.domain.restaurant.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.PostSearchService;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantSearchService implements PostSearchService {

  private final RestaurantRepository restaurantRepository;

  @Override
  public Post getPost(Long postId, Category category) {
    return restaurantRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }
}
