package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketCardService implements PostCardService {

  private final MarketRepository marketRepository;

  @Override
  public PostCardDto getPostCardDto(Long postId, Language language) {
    PostCardDto postCardDto = marketRepository.findPostCardDto(postId, language);
    Optional.ofNullable(postCardDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.MARKET.toString());
    return postCardDto;
  }
}
