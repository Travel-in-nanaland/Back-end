package com.jeju.nanaland.domain.nature.service;

import static com.jeju.nanaland.domain.common.data.Category.NATURE;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureDetailDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
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
   * 카드 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostCardDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 7대자연 정보가 존재하지 않는 경우
   */
  @Override
  public PostCardDto getPostCardDto(Long postId, Category category, Language language) {
    PostCardDto postCardDto = natureRepository.findPostCardDto(postId, language);
    Optional.ofNullable(postCardDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.NATURE.toString());
    return postCardDto;
  }

  // 7대 자연 리스트 조회
  public NatureThumbnailDto getNatureList(MemberInfoDto memberInfoDto,
      List<String> addressFilterList, String keyword, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureThumbnail> natureCompositeDtoPage = natureRepository.findNatureThumbnails(
        memberInfoDto.getLanguage(), addressFilterList, keyword, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());

    List<NatureThumbnail> data = natureCompositeDtoPage.getContent();
    for (NatureThumbnail natureThumbnail : data) {
      natureThumbnail.setFavorite(favoriteIds.contains(natureThumbnail.getId()));
    }

    return NatureThumbnailDto.builder()
        .totalElements(natureCompositeDtoPage.getTotalElements())
        .data(data)
        .build();
  }

  // 7대 자연 상세 정보 조회
  public NatureDetailDto getNatureDetail(MemberInfoDto memberInfoDto, Long id, boolean isSearch) {
    NatureCompositeDto natureCompositeDto = natureRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage());

    if (natureCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(NATURE, id);
    }

    // 좋아요 여부 확인
    boolean isFavorite =
        memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), NATURE, id);

    return NatureDetailDto.builder()
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
        .images(imageFileService.getPostImageFilesByPostIdIncludeFirstImage(id,
            natureCompositeDto.getFirstImage()))
        .build();
  }
}
