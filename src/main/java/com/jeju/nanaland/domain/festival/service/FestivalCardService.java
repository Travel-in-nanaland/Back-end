package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalCardService implements PostCardService {

  private final FestivalRepository festivalRepository;

  @Override
  public PostCardDto getPostCardDto(Long postId, Language language) {
    PostCardDto postCardDto = festivalRepository.findPostCardDto(postId, language);
    Optional.ofNullable(postCardDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.FESTIVAL.toString());
    return postCardDto;
  }
}
