package com.jeju.nanaland.domain.experience.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperienceService {

  private final ExperienceRepository experienceRepository;
  private final FavoriteService favoriteService;

  public ExperienceThumbnailDto getExperienceList(MemberInfoDto memberInfoDto,
      ExperienceType experienceType, List<String> keywordFilterList, List<String> addressFilterList,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);
    // experienceType(액티비티, 문화예술)에 따른 이색체험 조회
    Page<ExperienceThumbnail> experienceThumbnailPage = experienceRepository.findExperienceThumbnails(
        language, experienceType, keywordFilterList, addressFilterList, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());
    List<ExperienceThumbnail> data = experienceThumbnailPage.getContent();
    // favorite에 해당 id가 존재하면 isFavorite 필드 true, 아니라면 false
    for (ExperienceThumbnail experienceThumbnail : data) {
      experienceThumbnail.setFavorite(favoriteIds.contains(experienceThumbnail.getId()));
    }

    // TODO: 리뷰 평점 평균

    return ExperienceThumbnailDto.builder()
        .totalElements(experienceThumbnailPage.getTotalElements())
        .data(data)
        .build();
  }
}
