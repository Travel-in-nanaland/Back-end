package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberTypeService {

  private final MemberTravelTypeRepository memberTravelTypeRepository;
  private final RecommendRepository recommendRepository;
  private final FavoriteRepository favoriteRepository;

  @Transactional
  public void updateMemberType(MemberInfoDto memberInfoDto, UpdateTypeDto updateTypeDto) {

    Member member = memberInfoDto.getMember();
    String newTravelType = updateTypeDto.getType();
    MemberTravelType newType = memberTravelTypeRepository.findByTravelType(
        TravelType.valueOf(newTravelType));

    // Enum에는 있지만 DB에는 없는 경우
    if (newType == null) {
      String errorMessage = newTravelType + "에 해당하는 타입 정보가 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    member.updateMemberTravelType(newType);
  }

  public List<RecommendPostDto> getRecommendResultPostsByType(MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();
    MemberTravelType memberTravelType = member.getMemberTravelType();
    TravelType travelType = member.getMemberTravelType().getTravelType();
    if (travelType == TravelType.NONE) {
      // 타입이 NONE이면 랜덤으로 NONE이 아닌 하나의 타입 선택
      memberTravelType = memberTravelTypeRepository.findByTravelType(getRandomTravelType());
      travelType = memberTravelType.getTravelType();
    }

    List<Recommend> recommends = recommendRepository.findAllByMemberTravelType(memberTravelType);
    if (recommends == null || recommends.size() < 2) {
      String errorMessage = travelType.name() + "에 해당하는 추천 게시물이 없거나 너무 적습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    recommends = getRandomTwoPosts(recommends);
    List<RecommendPostDto> result = new ArrayList<>();
    for (Recommend recommend : recommends) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();

      // recommend 테이블의 이미지를 사용
      result.add(getRecommendResultPostDto(member, postId, locale, travelType, category));
    }

    return result;
  }

  public List<RecommendPostDto> getRandomRecommendedPosts(MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    Locale locale = memberInfoDto.getLanguage().getLocale();

    // 이색체험 제외하고 모두 조회
    List<Recommend> recommends = recommendRepository.findAllWithoutExperience();
    if (recommends == null || recommends.size() < 2) {
      String errorMessage = "추천 게시물이 없거나 너무 적습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }
    recommends = getRandomTwoPosts(recommends);

    List<RecommendPostDto> result = new ArrayList<>();
    for (Recommend recommend : recommends) {
      Long postId = recommend.getPostId();
      Category category = recommend.getCategory();
      TravelType travelType = recommend.getMemberTravelType().getTravelType();

      // 해당 포스트의 이미지를 사용
      result.add(getRecommendPostDto(member, postId, locale, travelType, category));
    }

    return result;
  }

  private RecommendPostDto getRecommendPostDto(Member member, Long postId, Locale locale,
      TravelType travelType, Category category) {

    CategoryContent categoryContent = category.getContent();
    RecommendPostDto recommendPostDto = switch (categoryContent) {
      case NATURE -> recommendRepository.findNatureRecommendPostDto(postId, locale, travelType);

      case FESTIVAL -> recommendRepository.findFestivalRecommendPostDto(postId, locale, travelType);

      case EXPERIENCE ->
          recommendRepository.findExperienceRecommendPostDto(postId, locale, travelType);

      case MARKET -> recommendRepository.findMarketRecommendPostDto(postId, locale, travelType);

      case NANA -> recommendRepository.findNanaRecommendPostDto(postId, locale, travelType);

      default -> throw new NotFoundException("해당 추천 게시물 정보가 존재하지 않습니다.");
    };

    if (recommendPostDto == null) {
      String errorMessage = postId + ", " + category.getContent().name() + "게시물이 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    Optional<Favorite> favorite = favoriteRepository.findByMemberAndCategoryAndPostId(member,
        category, postId);
    if (favorite.isPresent()) {
      recommendPostDto.setFavorite(true);
    }

    return recommendPostDto;
  }

  private RecommendPostDto getRecommendResultPostDto(Member member, Long postId, Locale locale,
      TravelType travelType, Category category) {

    CategoryContent categoryContent = category.getContent();
    RecommendPostDto recommendResultPostDto = switch (categoryContent) {
      case NATURE ->
          recommendRepository.findNatureRecommendResultPostDto(postId, locale, travelType);

      case FESTIVAL ->
          recommendRepository.findFestivalRecommendResultPostDto(postId, locale, travelType);

      case EXPERIENCE ->
          recommendRepository.findExperienceRecommendResultPostDto(postId, locale, travelType);

      case MARKET ->
          recommendRepository.findMarketRecommendResultPostDto(postId, locale, travelType);

      case NANA -> recommendRepository.findNanaRecommendResultPostDto(postId, locale, travelType);

      default -> throw new NotFoundException("해당 추천 게시물 정보가 존재하지 않습니다.");
    };

    if (recommendResultPostDto == null) {
      String errorMessage = postId + ", " + category.getContent().name() + "게시물이 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    Optional<Favorite> favorite = favoriteRepository.findByMemberAndCategoryAndPostId(member,
        category, postId);
    if (favorite.isPresent()) {
      recommendResultPostDto.setFavorite(true);
    }

    return recommendResultPostDto;
  }

  private TravelType getRandomTravelType() {
    Random random = new Random();
    List<TravelType> values = new ArrayList<>(List.of(TravelType.values()));
    values.remove(TravelType.NONE);

    return values.get(random.nextInt(values.size()));
  }

  private List<Recommend> getRandomTwoPosts(List<Recommend> recommends) {
    if (recommends.size() == 2) {
      return recommends;
    }

    int randomIdx;
    List<Recommend> result = new ArrayList<>();
    Random random = new Random();
    randomIdx = random.nextInt(recommends.size());
    result.add(recommends.get(randomIdx));
    recommends.remove(randomIdx);

    randomIdx = random.nextInt(recommends.size());
    result.add(recommends.get(randomIdx));

    return result;
  }
}
