package com.jeju.nanaland.domain.experience.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceDetailDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
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
  private final ImageFileRepository imageFileRepository;
  private final SearchService searchService;

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

  public ExperienceDetailDto getExperienceDetails(MemberInfoDto memberInfoDto, Long postId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();
    ExperienceCompositeDto experienceCompositeDto = experienceRepository.findCompositeDtoById(
        postId, language);

    // 해당 id의 포스트가 없는 경우 404 에러
    if (experienceCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(EXPERIENCE, postId);
    }

    // 좋아요 여부 확인
    Member member = memberInfoDto.getMember();
    boolean isFavorite = favoriteService.isPostInFavorite(member, EXPERIENCE, postId);

    // 이미지
    List<ImageFileDto> images = new ArrayList<>();
    images.add(experienceCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(postId));

    return ExperienceDetailDto.builder()
        .id(experienceCompositeDto.getId())
        .title(experienceCompositeDto.getTitle())
        .content(experienceCompositeDto.getContent())
        .address(experienceCompositeDto.getAddress())
        .addressTag(experienceCompositeDto.getAddressTag())
        .contact(experienceCompositeDto.getContact())
        .homepage(experienceCompositeDto.getHomepage())
        .time(experienceCompositeDto.getTime())
        .amenity(experienceCompositeDto.getAmenity())
        .details(experienceCompositeDto.getDetails())
        .isFavorite(isFavorite)
        .images(images)
        .build();
  }
}
