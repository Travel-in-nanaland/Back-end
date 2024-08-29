package com.jeju.nanaland.domain.favorite.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.FavoriteThumbnailDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final NanaRepository nanaRepository;
  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final FestivalRepository festivalRepository;
  private final MarketRepository marketRepository;
  private final RestaurantRepository restaurantRepository;

  // 전체 찜리스트 조회
  public FavoriteThumbnailDto getAllFavoriteList(MemberInfoDto memberInfoDto, int page, int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // Favorite 테이블에서 해당 유저의 찜리스트 페이지 조회
    Page<Favorite> favorites = favoriteRepository
        .findAllByMemberAndStatusOrderByCreatedAtDesc(member, "ACTIVE", pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();

    // Favorite의 postId, 카테고리 정보를 통해 썸네일 정보 조회
    for (Favorite favorite : favorites) {
      Category category = favorite.getCategory();
      Long postId = favorite.getPost().getId();

      thumbnailDtoList.add(getThumbnailDto(member, postId, language, category));
    }

    return FavoriteThumbnailDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  // 카테고리별 찜리스트 조회
  public FavoriteThumbnailDto getCategoryFavoriteList(MemberInfoDto memberInfoDto,
      Category categoryContent, int page, int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // 해당 카테고리의 찜리스트 조회
    Page<ThumbnailDto> thumbnails = getThumbnailDtoPage(member, language, pageable,
        categoryContent);
    List<ThumbnailDto> thumbnailDtoList = thumbnails.getContent();

    return FavoriteThumbnailDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  // 좋아요 토글
  @Transactional
  public FavoriteResponse.StatusDto toggleLikeStatus(MemberInfoDto memberInfoDto,
      FavoriteRequest.LikeToggleDto likeToggleDto) {

    Long postId = likeToggleDto.getId();
    Category category = Category.valueOf(likeToggleDto.getCategory());

    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        memberInfoDto.getMember(), category, postId);

    // favorite 엔티티에 존재
    if (favoriteOptional.isPresent()) {
      Favorite favorite = favoriteOptional.get();

      // 찜 상태라면 찜 취소
      if (favorite.isStatusActive()) {
        favorite.setStatusInactive();

        return FavoriteResponse.StatusDto.builder()
            .isFavorite(false)
            .build();
      }
      // 찜 취소 상태라면 찜 등록
      else {
        favorite.setStatusActive();

        return FavoriteResponse.StatusDto.builder()
            .isFavorite(true)
            .build();
      }
    }
    // favorite 엔티티에 존재하지 않음: 처음 찜 목록에 추가하는 경우
    else {
      // 해당 카테고리에 해당하는 id의 게시물이 없다면 NotFoundException
      Post post = findPostIfExist(postId, category);

      Favorite favorite = Favorite.builder()
          .member(memberInfoDto.getMember())
          .category(category)
          .post(post)
          .build();

      // 좋아요 추가
      favoriteRepository.save(favorite);

      return FavoriteResponse.StatusDto.builder()
          .isFavorite(true)
          .build();
    }
  }

  // 해당 유저의 찜리스트에 있는 postId 리스트 반환
  public List<Long> getFavoritePostIdsWithMember(Member member) {
    List<Favorite> favorites = favoriteRepository.findAllByMemberAndStatus(member, "ACTIVE");
    return favorites.stream().map(favorite -> favorite.getPost().getId()).toList();
  }

  // 좋아요 여부 조회
  public boolean isPostInFavorite(Member member, Category category, Long id) {
    Optional<Favorite> favoriteOptional = favoriteRepository
        .findByMemberAndCategoryAndPostIdAndStatus(member, category, id, "ACTIVE");

    return favoriteOptional.isPresent();
  }

  // 카테고리별 게시물 썸네일 정보 조회
  public ThumbnailDto getThumbnailDto(Member member, Long postId, Language locale,
      Category category) {
    return switch (category) {
      case NANA -> favoriteRepository.findNanaThumbnailByPostId(member, postId, locale);
      case NATURE -> favoriteRepository.findNatureThumbnailByPostId(member, postId, locale);
      case MARKET -> favoriteRepository.findMarketThumbnailByPostId(member, postId, locale);
      case EXPERIENCE -> favoriteRepository.findExperienceThumbnailByPostId(member, postId, locale);
      case FESTIVAL -> favoriteRepository.findFestivalThumbnailByPostId(member, postId, locale);
      case RESTAURANT -> favoriteRepository.findRestaurantThumbnailByPostId(member, postId, locale);
      default -> null;
    };
  }

  // 카테고리별 게시물 썸네일 정보 조회 (페이징)
  private Page<ThumbnailDto> getThumbnailDtoPage(Member member, Language locale, Pageable pageable,
      Category categoryContent) {
    return switch (categoryContent) {
      case NANA -> favoriteRepository.findNanaThumbnails(member, locale, pageable);
      case EXPERIENCE -> favoriteRepository.findExperienceThumbnails(member, locale, pageable);
      case NATURE -> favoriteRepository.findNatureThumbnails(member, locale, pageable);
      case MARKET -> favoriteRepository.findMarketThumbnails(member, locale, pageable);
      case FESTIVAL -> favoriteRepository.findFestivalThumbnails(member, locale, pageable);
      case RESTAURANT -> favoriteRepository.findRestaurantThumbnails(member, locale, pageable);
      default -> throw new NotFoundException(CATEGORY_NOT_FOUND.getMessage());
    };
  }

  // 게시물 조회
  private Post findPostIfExist(Long postId, Category category) {
    return switch (category) {
      case NANA -> nanaRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      case NATURE -> natureRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      case EXPERIENCE -> experienceRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      case MARKET -> marketRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      case FESTIVAL -> festivalRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      case RESTAURANT -> restaurantRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));

      default -> throw new NotFoundException(CATEGORY_NOT_FOUND.getMessage());
    };
  }
}
