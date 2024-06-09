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

  public FavoriteResponse.AllCategoryDto getAllFavoriteList(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    // Favorite 테이블에서 유저 id에 해당하는 튜플 모두 조회
    Page<Favorite> favorites = favoriteRepository.findAllCategoryFavorite(member, pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();

    // Favorite의 postId, 카테고리 정보를 통해 튜플 하나하나 조회
    for (Favorite favorite : favorites) {
      CategoryContent category = favorite.getCategory().getContent();
      Long postId = favorite.getPost().getId();
      log.info("postId: {}", postId);

      switch (category) {
        case NANA -> {
          ThumbnailDto thumbnailDto = favoriteRepository.findNanaThumbnailByPostId(member, postId,
              locale);
          thumbnailDto.setCategory(NANA.name());
          thumbnailDtoList.add(thumbnailDto);
        }
        case NATURE -> {
          ThumbnailDto thumbnailDto = favoriteRepository.findNatureThumbnailByPostId(member, postId,
              locale);
          thumbnailDto.setCategory(NATURE.name());
          thumbnailDtoList.add(thumbnailDto);
        }
        case MARKET -> {
          ThumbnailDto thumbnailDto = favoriteRepository.findMarketThumbnailByPostId(member, postId,
              locale);
          thumbnailDto.setCategory(MARKET.name());
          thumbnailDtoList.add(thumbnailDto);
        }
        case EXPERIENCE -> {
          ThumbnailDto thumbnailDto = favoriteRepository.findExperienceThumbnailByPostId(member,
              postId,
              locale);
          thumbnailDto.setCategory(EXPERIENCE.name());
          thumbnailDtoList.add(thumbnailDto);
        }
        case FESTIVAL -> {
          ThumbnailDto thumbnailDto = favoriteRepository.findFestivalThumbnailByPostId(member,
              postId,
              locale);
          thumbnailDto.setCategory(FESTIVAL.name());
          thumbnailDtoList.add(thumbnailDto);
        }
      }
    }

    return FavoriteResponse.AllCategoryDto.builder()
        .totalElements(favorites.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteResponse.NatureDto getNatureFavoriteList(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    Page<ThumbnailDto> thumbnails = favoriteRepository.findNatureThumbnails(member, locale,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();
    for (ThumbnailDto thumbnailDto : thumbnails) {
      thumbnailDto.setCategory(NATURE.name());
      thumbnailDtoList.add(thumbnailDto);
    }

    return FavoriteResponse.NatureDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteResponse.FestivalDto getFestivalFavoriteList(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    Page<ThumbnailDto> thumbnails = favoriteRepository.findFestivalThumbnails(member, locale,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();
    for (ThumbnailDto thumbnailDto : thumbnails) {
      thumbnailDto.setCategory(FESTIVAL.name());
      thumbnailDtoList.add(thumbnailDto);
    }

    return FavoriteResponse.FestivalDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteResponse.ExperienceDto getExperienceFavoriteList(MemberInfoDto memberInfoDto,
      int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    Page<ThumbnailDto> thumbnails = favoriteRepository.findExperienceThumbnails(member, locale,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();
    for (ThumbnailDto thumbnailDto : thumbnails) {
      thumbnailDto.setCategory(EXPERIENCE.name());
      thumbnailDtoList.add(thumbnailDto);
    }

    return FavoriteResponse.ExperienceDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteResponse.MarketDto getMarketFavoriteList(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    Page<ThumbnailDto> thumbnails = favoriteRepository.findMarketThumbnails(member, locale,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();
    for (ThumbnailDto thumbnailDto : thumbnails) {
      thumbnailDto.setCategory(MARKET.name());
      thumbnailDtoList.add(thumbnailDto);
    }

    return FavoriteResponse.MarketDto.builder()
        .totalElements(thumbnails.getTotalElements())
        .data(thumbnailDtoList)
        .build();
  }

  public FavoriteResponse.NanaDto getNanaFavoriteList(MemberInfoDto memberInfoDto, int page,
      int size) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    Pageable pageable = PageRequest.of(page, size);

    Page<ThumbnailDto> thumbnails = favoriteRepository.findNanaThumbnails(member, locale,
        pageable);
    List<ThumbnailDto> thumbnailDtoList = new ArrayList<>();
    for (ThumbnailDto thumbnailDto : thumbnails) {
      thumbnailDto.setCategory(NANA.name());
      thumbnailDtoList.add(thumbnailDto);
    }

    return FavoriteResponse.NanaDto.builder()
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

  public List<Long> getMemberFavoritePostIds(Member member, CategoryContent categoryContent) {

    Category category = getCategoryFromCategoryContent(categoryContent);
    List<Favorite> favorites = favoriteRepository.findAllByMemberAndCategory(member, category);

    List<Long> postIds = new ArrayList<>();
    for (Favorite favorite : favorites) {
      postIds.add(favorite.getPost().getId());
    }
    return postIds;
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

  public boolean isPostInFavorite(Member member, CategoryContent categoryContent, Long id) {
    Category category = getCategoryFromCategoryContent(categoryContent);
    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        member, category, id);

    return favoriteOptional.isPresent();
  }

  private Post findPostIfExist(Long postId, CategoryContent categoryContent) {
    return switch (categoryContent) {
      // TODO: NANA Post 엔티티 상속 이후 수정
//      case NANA -> nanaRepository.findById(postId)
//          .orElseThrow(() -> new NotFoundException("해당 id의 나나스픽 게시물이 존재하지 않습니다."));

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
