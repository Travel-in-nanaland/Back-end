package com.jeju.nanaland.domain.experience.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperienceService {

  private final ExperienceRepository experienceRepository;
  private final FavoriteService favoriteService;

  @Transactional
  public FavoriteResponse.StatusDto toggleLikeStatus(MemberInfoDto memberInfoDto, Long postId) {
    experienceRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 이색체험 게시물이 존재하지 않습니다."));

    Boolean status = favoriteService.toggleLikeStatus(memberInfoDto.getMember(),
        CategoryContent.EXPERIENCE, postId);
    return FavoriteResponse.StatusDto.builder()
        .isFavorite(status)
        .build();
  }
}
