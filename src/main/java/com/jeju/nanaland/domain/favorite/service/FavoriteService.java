package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.PreviewDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
  private final FestivalRepository festivalRepository;

  /**
   * 모든 카테고리의 찜리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param page          페이지
   * @param size          페이지 크기
   * @return FavoriteResponse.PreviewPageDto
   */
  @Transactional(readOnly = true)
  public FavoriteResponse.PreviewPageDto getAllFavorites(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // favorite 테이블에서 해당 유저의 찜리스트 페이지 조회
    Page<Favorite> favorites =
        favoriteRepository.findAllFavoritesOrderByModifiedAtDesc(member, pageable);

    // 조회된 Favorite 객체 리스트를 통해 FavoritePostCardDto 정보 가져오기
    List<FavoriteResponse.PreviewDto> favoritePreviewDtos = favorites.stream()
        .map(favorite -> {
          Category category = favorite.getCategory();
          Long postId = favorite.getPost().getId();
          PostPreviewDto postPreviewDto = postService.getPostPreviewDto(postId, category, language);
          if (category == Category.FESTIVAL) {
            setFestivalOnGoingStatus(postPreviewDto);
          }
          return new PreviewDto(postPreviewDto);
        })
        .collect(Collectors.toList());

    return FavoriteResponse.PreviewPageDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(favoritePreviewDtos)
        .build();
  }

  /**
   * 카테고리별 찜리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param category      게시물 카테고리
   * @param page          페이지
   * @param size          페이지 크기
   * @return FavoriteResponse.PreviewPageDto
   */
  @Transactional(readOnly = true)
  public FavoriteResponse.PreviewPageDto getAllCategoryFavorites(MemberInfoDto memberInfoDto,
      Category category, int page, int size) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // favorite 테이블에서 해당 유저의 카테고리 찜리스트 페이지 조회
    Page<Favorite> favoritePage =
        favoriteRepository.findAllFavoritesOrderByModifiedAtDesc(member, category, pageable);

    // 각 게시물 조회 쿼리 비동기 요청
    List<CompletableFuture<FavoriteResponse.PreviewDto>> favoritePreviewFutures = favoritePage.get()
        .map(favorite -> CompletableFuture.supplyAsync(() -> {
          Long postId = favorite.getPost().getId();
          PostPreviewDto postPreviewDto = postService.getPostPreviewDto(postId, category, language);
          if (category == Category.FESTIVAL) {
            setFestivalOnGoingStatus(postPreviewDto);
          }
          return new FavoriteResponse.PreviewDto(postPreviewDto);
        }))
        .toList();

    // 모든 비동기 작업이 완료될 때까지 기다린 후 각 결과를 수집
    List<FavoriteResponse.PreviewDto> favoritePreviewDtos = CompletableFuture.allOf(
        favoritePreviewFutures.toArray(new CompletableFuture[0])
    ).thenApply(v ->
        favoritePreviewFutures.stream()
            .map(CompletableFuture::join) // 순서대로 결과 가져오기
            .collect(Collectors.toList())
    ).join(); // 최종 결과 리스트

    return FavoriteResponse.PreviewPageDto.builder()
        .totalElements(favoritePage.getTotalElements())
        .data(favoritePreviewDtos)
        .build();
  }

  /**
   * 이색체험 찜리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param page          페이지
   * @param size          페이지 크기
   * @return FavoriteResponse.PreviewPageDto
   */
  @Transactional(readOnly = true)
  public FavoriteResponse.PreviewPageDto getExperienceFavorites(MemberInfoDto memberInfoDto,
      ExperienceType experienceType, int page, int size) {

    Member member = memberInfoDto.getMember();
    Category category = Category.EXPERIENCE;
    Language language = memberInfoDto.getLanguage();
    Pageable pageable = PageRequest.of(page, size);

    // favorite 테이블에서 해당 유저의 이색체험(액티비티, 문화예술) 찜리스트 페이지 조회
    Page<Favorite> favoritePage =
        favoriteRepository.findAllExperienceFavoritesOrderByModifiedAtDesc(member, experienceType
            , pageable);

    // 각 게시물 조회 쿼리 비동기 요청
    List<CompletableFuture<FavoriteResponse.PreviewDto>> favoritePreviewFutures = favoritePage.get()
        .map(favorite -> CompletableFuture.supplyAsync(() -> {
          Long postId = favorite.getPost().getId();
          PostPreviewDto postPreviewDto = postService.getPostPreviewDto(postId, category, language);
          return new FavoriteResponse.PreviewDto(postPreviewDto);
        }))
        .toList();

    // 모든 비동기 작업이 완료될 때까지 기다린 후 각 결과를 수집
    List<FavoriteResponse.PreviewDto> favoritePreviewDtos = CompletableFuture.allOf(
        favoritePreviewFutures.toArray(new CompletableFuture[0])
    ).thenApply(v ->
        favoritePreviewFutures.stream()
            .map(CompletableFuture::join) // 순서대로 결과 가져오기
            .collect(Collectors.toList())
    ).join(); // 최종 결과 리스트

    return FavoriteResponse.PreviewPageDto.builder()
        .totalElements(favoritePage.getTotalElements())
        .data(favoritePreviewDtos)
        .build();
  }

  /**
   * 찜 등록, 취소 토글
   *
   * @param memberInfoDto 회원 정보
   * @param likeToggleDto 찜 요청, 삭제 정보
   * @return FavoriteResponse.StatusDto
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

  private void setFestivalOnGoingStatus(PostPreviewDto postPreviewDto) {
    Long id = postPreviewDto.getId();
    postPreviewDto.setOnGoing(festivalRepository.getFestivalOnGoingStatusById(id));
  }
}
