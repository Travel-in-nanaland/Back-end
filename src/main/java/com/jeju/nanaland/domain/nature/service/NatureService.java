package com.jeju.nanaland.domain.nature.service;

import static com.jeju.nanaland.domain.common.data.Category.NATURE;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NatureService implements PostService {

  private final NatureRepository natureRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final SearchService searchService;
  private final ImageFileService imageFileService;

  /**
   * Nature 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException 게시물 id에 해당하는 7대자연 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return natureRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }

  /**
   * 게시물 preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostPreviewDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 7대자연 정보가 존재하지 않는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {
    PostPreviewDto postPreviewDto = natureRepository.findPostPreviewDto(postId, language);
    Optional.ofNullable(postPreviewDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postPreviewDto.setCategory(PostCategory.NATURE.toString());
    return postPreviewDto;
  }

  /**
   * 7대 자연 프리뷰 리스트 조회
   *
   * @param memberInfoDto  회원 정보
   * @param addressFilters 지역명
   * @param keyword        키워드
   * @param page           페이지 Number
   * @param size           페이지 Size
   * @return 7대 자연 프리뷰 리스트
   */
  public NatureResponse.PreviewPageDto getNaturePreview(MemberInfoDto memberInfoDto,
      List<String> addressFilters, String keyword, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureResponse.PreviewDto> naturePreviewDto = natureRepository.findAllNaturePreviewDtoOrderByPriority(
        memberInfoDto.getLanguage(), addressFilters, keyword, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());

    List<NatureResponse.PreviewDto> data = naturePreviewDto.getContent();
    for (NatureResponse.PreviewDto natureThumbnail : data) {
      natureThumbnail.setFavorite(favoriteIds.contains(natureThumbnail.getId()));
    }

    return NatureResponse.PreviewPageDto.builder()
        .totalElements(naturePreviewDto.getTotalElements())
        .data(data)
        .build();
  }

  /**
   * 7대 자연 상세 정보 조회
   *
   * @param memberInfoDto 회원 정보
   * @param natureId      7대 자연 ID
   * @param isSearch      검색 여부
   * @return 7대 자연 상세 정보
   * @throws NotFoundException 존재하는 7대 자연이 없는 경우
   */
  public NatureResponse.DetailDto getNatureDetail(MemberInfoDto memberInfoDto, Long natureId,
      boolean isSearch) {
    NatureCompositeDto natureCompositeDto = natureRepository.findNatureCompositeDto(natureId,
        memberInfoDto.getLanguage());

    if (natureCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(NATURE, natureId);
    }

    // 좋아요 여부 확인
    boolean isFavorite =
        memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), NATURE, natureId);

    return NatureResponse.DetailDto.builder()
        .id(natureCompositeDto.getId())
        .addressTag(natureCompositeDto.getAddressTag())
        .title(natureCompositeDto.getTitle())
        .content(natureCompositeDto.getContent())
        .intro(natureCompositeDto.getIntro())
        .address(natureCompositeDto.getAddress())
        .contact(natureCompositeDto.getContact())
        .time(natureCompositeDto.getTime())
        .fee(natureCompositeDto.getFee())
        .details(natureCompositeDto.getDetails())
        .amenity(natureCompositeDto.getAmenity())
        .isFavorite(isFavorite)
        .images(imageFileService.getPostImageFilesByPostIdIncludeFirstImage(natureId,
            natureCompositeDto.getFirstImage()))
        .build();
  }
}
