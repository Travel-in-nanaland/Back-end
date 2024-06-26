package com.jeju.nanaland.domain.favorite.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.CategoryContent.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.CategoryRepository;
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
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
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

  private final CategoryRepository categoryRepository;
  private final FavoriteRepository favoriteRepository;

  private final NanaRepository nanaRepository;
  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final FestivalRepository festivalRepository;
  private final MarketRepository marketRepository;

  public FavoriteThumbnailDto getAllFavoriteList(MemberInfoDto memberInfoDto, int page, int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    // Favorite 테이블에서 해당 유저의 찜리스트 페이지 조회
    Page<Favorite> favorites = favoriteRepository.findAllByMemberOrderByCreatedAtDesc(member,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();

    // Favorite의 postId, 카테고리 정보를 통해 썸네일 정보 조회
    for (Favorite favorite : favorites) {
      CategoryContent category = favorite.getCategory().getContent();
      Long postId = favorite.getPost().getId();

      thumbnailDtoList.add(getThumbnailDto(member, postId, locale, category));
    }

    return FavoriteThumbnailDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteThumbnailDto getCategoryFavoriteList(MemberInfoDto memberInfoDto,
      CategoryContent categoryContent, int page, int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    // 해당 카테고리의 찜리스트 조회
    Page<ThumbnailDto> thumbnails = getThumbnailDtoPage(member, locale, pageable, categoryContent);
    List<ThumbnailDto> thumbnailDtoList = thumbnails.getContent();

    return FavoriteThumbnailDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  @Transactional
  public FavoriteResponse.StatusDto toggleLikeStatus(MemberInfoDto memberInfoDto,
      FavoriteRequest.LikeToggleDto likeToggleDto) {

    Long postId = likeToggleDto.getId();
    CategoryContent categoryContent = CategoryContent.valueOf(likeToggleDto.getCategory());
    Category category = getCategoryFromCategoryContent(categoryContent);

    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        memberInfoDto.getMember(), category, postId);

    // 좋아요 상태일 때
    if (favoriteOptional.isPresent()) {
      Favorite favorite = favoriteOptional.get();

      // 좋아요 삭제
      favoriteRepository.delete(favorite);

      return FavoriteResponse.StatusDto.builder()
          .isFavorite(false)
          .build();
    }
    // 좋아요 상태가 아닐 때
    else {
      // 해당 카테고리에 해당하는 id의 게시물이 없다면 NotFoundException
      Post post = findPostIfExist(postId, categoryContent);

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
    List<Favorite> favorites = favoriteRepository.findAllByMember(member);
    return favorites.stream().map(favorite -> favorite.getPost().getId()).toList();
  }

  public boolean isPostInFavorite(Member member, CategoryContent categoryContent, Long id) {
    Category category = getCategoryFromCategoryContent(categoryContent);
    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        member, category, id);

    return favoriteOptional.isPresent();
  }

  private Category getCategoryFromCategoryContent(CategoryContent categoryContent) {
    return switch (categoryContent) {
      case NANA -> categoryRepository.findByContent(NANA)
          .orElseThrow(() -> new ServerErrorException("NANA에 해당하는 카테고리가 없습니다."));

      case EXPERIENCE -> categoryRepository.findByContent(EXPERIENCE)
          .orElseThrow(() -> new ServerErrorException("EXPERIENCE에 해당하는 카테고리가 없습니다."));

      case NATURE -> categoryRepository.findByContent(NATURE)
          .orElseThrow(() -> new ServerErrorException("NATURE에 해당하는 카테고리가 없습니다."));

      case MARKET -> categoryRepository.findByContent(MARKET)
          .orElseThrow(() -> new ServerErrorException("MARKET에 해당하는 카테고리가 없습니다."));

      case FESTIVAL -> categoryRepository.findByContent(FESTIVAL)
          .orElseThrow(() -> new ServerErrorException("FESTIVAL에 해당하는 카테고리가 없습니다."));

      default -> throw new ServerErrorException("해당하는 카테고리가 없습니다.");
    };
  }

  private ThumbnailDto getThumbnailDto(Member member, Long postId, Locale locale,
      CategoryContent category) {
    return switch (category) {
      case NANA -> favoriteRepository.findNanaThumbnailByPostId(member, postId, locale);
      case NATURE -> favoriteRepository.findNatureThumbnailByPostId(member, postId, locale);
      case MARKET -> favoriteRepository.findMarketThumbnailByPostId(member, postId, locale);
      case EXPERIENCE -> favoriteRepository.findExperienceThumbnailByPostId(member, postId, locale);
      case FESTIVAL -> favoriteRepository.findFestivalThumbnailByPostId(member, postId, locale);
      default -> null;
    };
  }

  private Page<ThumbnailDto> getThumbnailDtoPage(Member member, Locale locale, Pageable pageable,
      CategoryContent categoryContent) {
    return switch (categoryContent) {
      case NANA -> favoriteRepository.findNanaThumbnails(member, locale, pageable);
      case EXPERIENCE -> favoriteRepository.findExperienceThumbnails(member, locale, pageable);
      case NATURE -> favoriteRepository.findNatureThumbnails(member, locale, pageable);
      case MARKET -> favoriteRepository.findMarketThumbnails(member, locale, pageable);
      case FESTIVAL -> favoriteRepository.findFestivalThumbnails(member, locale, pageable);
      default -> throw new ServerErrorException("해당하는 카테고리가 없습니다.");
    };
  }

  private Post findPostIfExist(Long postId, CategoryContent categoryContent) {
    return switch (categoryContent) {
      case NANA -> nanaRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException("해당 id의 나나스픽 게시물이 존재하지 않습니다."));

      case NATURE -> natureRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException("해당 id의 7대자연 게시물이 존재하지 않습니다."));

      case EXPERIENCE -> experienceRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException("해당 id의 이색체험 게시물이 존재하지 않습니다."));

      case MARKET -> marketRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException("해당 id의 전통시장 게시물이 존재하지 않습니다."));

      case FESTIVAL -> festivalRepository.findById(postId)
          .orElseThrow(() -> new NotFoundException("해당 id의 축제 게시물이 존재하지 않습니다."));

      default -> throw new NotFoundException("해당 id의 게시물이 존재하지 않습니다.");
    };
  }
}
