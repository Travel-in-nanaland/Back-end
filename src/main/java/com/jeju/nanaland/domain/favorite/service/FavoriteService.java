package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
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

  private final FavoritePostCardService favoritePostCardService;
  private final FavoriteRepository favoriteRepository;

  // 전체 찜리스트 조회
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
          return favoritePostCardService.getFavoritePostCardDto(postId, language, category);
        })
        .collect(Collectors.toList());

    return FavoriteResponse.FavoriteCardPageDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(favoritePostCardDtos)
        .build();
  }

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
        .map(favorite ->
            favoritePostCardService.getFavoritePostCardDto(favorite.getId(), language, category))
        .toList();

    return FavoriteResponse.FavoriteCardPageDto.builder()
        .totalElements(favoritePage.getTotalElements())
        .data(favoritePostCardDtos)
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
      Favorite favorite = Favorite.builder()
          .member(memberInfoDto.getMember())
          .category(category)
          // TODO
          //.post()
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
}
