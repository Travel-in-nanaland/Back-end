package com.jeju.nanaland.domain.nature.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NatureCardService implements PostCardService {

  private final NatureRepository natureRepository;

  @Override
  public PostCardDto getPostCardDto(Long postId, Language language) {
    PostCardDto postCardDto = natureRepository.findPostCardDto(postId, language)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.NATURE.toString());
    return postCardDto;
  }
}
