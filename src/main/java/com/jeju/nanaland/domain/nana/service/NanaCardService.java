package com.jeju.nanaland.domain.nana.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostCardService;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NanaCardService implements PostCardService {

  private final NanaRepository nanaRepository;

  @Override
  public PostCardDto getPostCardDto(Long postId, Language language) {
    PostCardDto postCardDto = nanaRepository.findPostCardDto(postId, language);

    // 게시물 정보가 없는 경우 에러처리
    Optional.ofNullable(postCardDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.NANA.toString());
    return postCardDto;
  }
}
