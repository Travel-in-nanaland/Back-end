package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.dto.FavoritePostCardDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
  private final PostService postService;

  /**
   * 모든 카테고리의 찜리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param page          페이지
   * @param size          페이지 크기
   * @return 찜 목록 카드 정보 페이지
   */
  @Transactional(readOnly = true)
  public FavoriteResponse.FavoriteCardPageDto getAllFavorites(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // favorite 테이블에서 해당 유저의 찜리스트 페이지 조회
    Page<Favorite> favorites =
        favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(member, pageable);

    // 조회된 Favorite 객체 리스트를 통해 FavoritePostCardDto 정보 가져오기
    List<FavoritePostCardDto> favoritePostCardDtos = favorites.stream()
        .map(favorite -> {
          Category category = favorite.getCategory();
          Long postId = favorite.getPost().getId();
          PostCardDto postCardDto = postService.getPostCardDto(postId, category, language);
          return new FavoritePostCardDto(postCardDto);
        })
        .collect(Collectors.toList());

    return FavoriteResponse.FavoriteCardPageDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(favoritePostCardDtos)
        .build();
  }

  /**
   * 카테고리별 찜리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param category      게시물 카테고리
   * @param page          페이지
   * @param size          페이지 크기
   * @return 찜 목록 카드 정보 페이지
   */
  @Transactional(readOnly = true)
  public FavoriteResponse.FavoriteCardPageDto getAllCategoryFavorites(MemberInfoDto memberInfoDto,
      Category category, int page, int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // favorite 테이블에서 해당 유저의 카테고리 찜리스트 페이지 조회
    Page<Favorite> favoritePage =
        favoriteRepository.findAllFavoritesOrderByCreatedAtDesc(member, category, pageable);

    // 조회된 Favorite 객체 리스트를 통해 FavoritePostCardDto 정보 가져오기
    List<FavoritePostCardDto> favoritePostCardDtos = favoritePage.get()
        .map(favorite -> {
          Long postId = favorite.getPost().getId();
          PostCardDto postCardDto = postService.getPostCardDto(postId, category, language);
          return new FavoritePostCardDto(postCardDto);
        })
        .toList();

    return FavoriteResponse.FavoriteCardPageDto.builder()
        .totalElements(favoritePage.getTotalElements())
        .data(favoritePostCardDtos)
        .build();
  }

  /**
   * 찜 등록, 취소 토글
   *
   * @param memberInfoDto 회원 정보
   * @param likeToggleDto 찜 요청, 삭제 정보
   * @return 찜 상태 정보
   */
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
      Favorite favorite = Favorite.builder()
          .member(memberInfoDto.getMember())
          .category(category)
          .post(postService.getPost(postId, category))
          .status("ACTIVE")
          .notificationCount(0)
          .build();

      // 좋아요 추가
      favoriteRepository.save(favorite);

      return FavoriteResponse.StatusDto.builder()
          .isFavorite(true)
          .build();
    }
  }

  /**
   * 유저 찜목록에 있는 게시물 id 리스트 조회
   *
   * @param member 회원
   * @return 게시물 id 리스트
   */
  public List<Long> getFavoritePostIdsWithMember(Member member) {
    List<Favorite> favorites = favoriteRepository.findAllByMemberAndStatus(member, "ACTIVE");
    return favorites.stream().map(favorite -> favorite.getPost().getId()).toList();
  }

  /**
   * 게시물의 찜 여부 확인
   *
   * @param member   회원
   * @param category 게시물 카테고리
   * @param postId   게시물 id
   * @return boolean
   */
  public boolean isPostInFavorite(Member member, Category category, Long postId) {
    Optional<Favorite> favoriteOptional = favoriteRepository
        .findByMemberAndCategoryAndPostIdAndStatus(member, category, postId, "ACTIVE");

    return favoriteOptional.isPresent();
  }
}
