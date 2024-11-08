package com.jeju.nanaland.domain.restaurant.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.Category.RESTAURANT;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.common.service.PostViewCountService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantDetailDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnailDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService implements PostService {

  private final RestaurantRepository restaurantRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final ReviewRepository reviewRepository;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;
  private final PostViewCountService postViewCountService;

  /**
   * Restaurant 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException 게시물 id에 해당하는 맛집 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return restaurantRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }

  /**
   * 게시물 preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostPreviewDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 맛집 정보가 존재하지 않는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {

    PostPreviewDto postPreviewDto = restaurantRepository.findPostPreviewDto(postId, language);
    Optional.ofNullable(postPreviewDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postPreviewDto.setCategory(PostCategory.RESTAURANT.toString());
    return postPreviewDto;
  }

  // 맛집 리스트 조회
  public RestaurantThumbnailDto getRestaurantList(MemberInfoDto memberInfoDto,
      List<RestaurantTypeKeyword> keywordFilterList, List<AddressTag> addressTags,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);
    Page<RestaurantThumbnail> restaurantThumbnailPage = restaurantRepository.findRestaurantThumbnails(
        language, keywordFilterList, addressTags, pageable);

    // 좋아요 여부
    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(
        memberInfoDto.getMember());
    List<RestaurantThumbnail> data = restaurantThumbnailPage.getContent();

    // 좋아요 여부, 리뷰 평균 추가
    for (RestaurantThumbnail restaurantThumbnail : data) {
      Long postId = restaurantThumbnail.getId();
      restaurantThumbnail.setFavorite(favoriteIds.contains(postId));
      restaurantThumbnail.setRatingAvg(reviewRepository.findTotalRatingAvg(RESTAURANT, postId));
    }

    return RestaurantThumbnailDto.builder()
        .totalElements(restaurantThumbnailPage.getTotalElements())
        .data(data)
        .build();
  }

  // 맛집 상세 정보 조회
  @Transactional
  public RestaurantDetailDto getRestaurantDetail(MemberInfoDto memberInfoDto, Long postId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();
    RestaurantCompositeDto restaurantCompositeDto = restaurantRepository.findCompositeDtoByIdWithPessimisticLock(
        postId, language);

    // 해당 id의 포스트가 없는 경우 404 에러
    if (restaurantCompositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    // 검색을 통해 요청되었다면 count
    if (isSearch) {
      searchService.updateSearchVolumeV1(EXPERIENCE, postId);
    }

    // 좋아요 여부 확인
    Member member = memberInfoDto.getMember();
    boolean isFavorite = memberFavoriteService.isPostInFavorite(member, RESTAURANT, postId);

    // 이미지
    List<ImageFileDto> images = new ArrayList<>();
    images.add(restaurantCompositeDto.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(postId));

    // 키워드
    Set<RestaurantTypeKeyword> keywordSet = restaurantRepository.getRestaurantTypeKeywordSetWithPessimisticLock(
        postId);
    List<String> keywords = keywordSet.stream()
        .map(experienceTypeKeyword ->
            experienceTypeKeyword.getValueByLocale(language)
        ).toList();

    // 메뉴
    List<RestaurantMenuDto> menuDtoList = restaurantRepository.getRestaurantMenuListWithPessimisticLock(
        postId,
        language);

    // 조회 수 증가
    postViewCountService.increaseViewCount(postId, member.getId());

    return RestaurantDetailDto.builder()
        .id(restaurantCompositeDto.getId())
        .title(restaurantCompositeDto.getTitle())
        .content(restaurantCompositeDto.getContent())
        .address(restaurantCompositeDto.getAddress())
        .addressTag(restaurantCompositeDto.getAddressTag())
        .contact(restaurantCompositeDto.getContact())
        .homepage(restaurantCompositeDto.getHomepage())
        .instagram(restaurantCompositeDto.getInstagram())
        .time(restaurantCompositeDto.getTime())
        .service(restaurantCompositeDto.getService())
        .keywords(keywords)
        .menus(menuDtoList)
        .isFavorite(isFavorite)
        .images(images)
        .build();
  }
}
