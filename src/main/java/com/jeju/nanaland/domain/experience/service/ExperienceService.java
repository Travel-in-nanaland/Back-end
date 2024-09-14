package com.jeju.nanaland.domain.experience.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceDetailDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperienceService implements PostService {

  private final ExperienceRepository experienceRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final ImageFileRepository imageFileRepository;
  private final SearchService searchService;
  private final ReviewRepository reviewRepository;

  /**
   * Experience 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException 게시물 id에 해당하는 이색체험 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return experienceRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }

  /**
   * 게시물 preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostPreviewDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 이색체험 정보가 존재하지 않는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {
    PostPreviewDto postPreviewDto = experienceRepository.findPostPreviewDto(postId, language);
    Optional.ofNullable(postPreviewDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postPreviewDto.setCategory(PostCategory.EXPERIENCE.toString());
    return postPreviewDto;
  }

  // 이색체험 리스트 조회
  public ExperienceThumbnailDto getExperienceList(MemberInfoDto memberInfoDto,
      ExperienceType experienceType, List<ExperienceTypeKeyword> keywordFilterList,
      List<String> addressFilterList, int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // experienceType(액티비티, 문화예술)에 따른 이색체험 조회
    Page<ExperienceThumbnail> experienceThumbnailPage = experienceRepository.findExperienceThumbnails(
        language, experienceType, keywordFilterList, addressFilterList, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());
    List<ExperienceThumbnail> data = experienceThumbnailPage.getContent();

    // 좋아요 여부, 리뷰 평균 추가
    for (ExperienceThumbnail experienceThumbnail : data) {
      Long postId = experienceThumbnail.getId();
      experienceThumbnail.setFavorite(favoriteIds.contains(postId));
      experienceThumbnail.setRatingAvg(reviewRepository.findTotalRatingAvg(EXPERIENCE, postId));
    }

    return ExperienceThumbnailDto.builder()
        .totalElements(experienceThumbnailPage.getTotalElements())
        .data(data)
        .build();
  }

  // 이색체험 상세 정보 조회
  public ExperienceDetailDto getExperienceDetail(MemberInfoDto memberInfoDto, Long postId,
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
    boolean isFavorite = memberFavoriteService.isPostInFavorite(member, EXPERIENCE, postId);

    // 이미지
    List<ImageFileDto> images = new ArrayList<>();
    images.add(experienceCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(postId));

    // 키워드
    Set<ExperienceTypeKeyword> keywordSet = experienceRepository.getExperienceTypeKeywordSet(
        postId);
    List<String> keywords = keywordSet.stream()
        .map(experienceTypeKeyword ->
            experienceTypeKeyword.getValueByLocale(language)
        ).toList();

    return ExperienceDetailDto.builder()
        .id(experienceCompositeDto.getId())
        .title(experienceCompositeDto.getTitle())
        .intro(experienceCompositeDto.getIntro())
        .content(experienceCompositeDto.getContent())
        .address(experienceCompositeDto.getAddress())
        .addressTag(experienceCompositeDto.getAddressTag())
        .contact(experienceCompositeDto.getContact())
        .homepage(experienceCompositeDto.getHomepage())
        .time(experienceCompositeDto.getTime())
        .amenity(experienceCompositeDto.getAmenity())
        .details(experienceCompositeDto.getDetails())
        .keywords(keywords)
        .isFavorite(isFavorite)
        .images(images)
        .build();
  }
}
