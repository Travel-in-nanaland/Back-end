package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.PostSearchService;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalSearchService implements PostSearchService {

  private final FestivalRepository festivalRepository;

  @Override
  public Post getPost(Long postId, Category category) {
    return festivalRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }
}
