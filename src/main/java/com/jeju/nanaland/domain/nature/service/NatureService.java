package com.jeju.nanaland.domain.nature.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NatureService {

  private final NatureRepository natureRepository;
  private final FavoriteService favoriteService;

  @Transactional
  public String toggleLikeStatus(Member member, Long postId) {
    natureRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 7대자연 게시물이 존재하지 않습니다."));

    return favoriteService.toggleLikeStatus(member, CategoryContent.NATURE, postId);
  }

  public NatureThumbnailDto getNatureList(MemberInfoDto memberInfoDto, String addressFilter,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureThumbnail> natureThumbnails = natureRepository.findNatureThumbnails(
        memberInfoDto.getLanguage().getLocale(), addressFilter, pageable);

    List<NatureThumbnail> data = natureThumbnails.getContent()
        .stream().map(natureThumbnail ->
            NatureThumbnail.builder()
                .id(natureThumbnail.getId())
                .title(natureThumbnail.getTitle())
                .thumbnailUrl(natureThumbnail.getThumbnailUrl())
                .address(natureThumbnail.getAddress())
                .build()).toList();

    return NatureThumbnailDto.builder()
        .totalElements(natureThumbnails.getTotalElements())
        .data(data)
        .build();
  }
}
