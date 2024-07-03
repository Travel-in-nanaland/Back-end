package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
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

  private final RecommendRepository recommendRepository;
  private final FavoriteRepository favoriteRepository;

  @Transactional
  public void updateMemberType(MemberInfoDto memberInfoDto, UpdateTypeDto updateTypeDto) {

    Member member = memberInfoDto.getMember();
    String newTravelType = updateTypeDto.getType();

    member.updateTravelType(TravelType.valueOf(newTravelType));
  }

  public List<RecommendPostDto> getRecommendPostsByType(MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    Language locale = memberInfoDto.getLanguage();
    TravelType travelType = member.getTravelType();

    // 타입이 NONE이면 랜덤으로 NONE이 아닌 하나의 타입 선택
    if (travelType == TravelType.NONE) {
      travelType = getRandomTravelType();
    }

    // 해당 여행 타입의 추천 게시물 모두 조회, 2개 이하라면 NotFoundException 반환
    List<Recommend> recommends = recommendRepository.findAllByTravelType(travelType);
    if (recommends == null || recommends.size() < 2) {
      String errorMessage = travelType.name() + "에 해당하는 추천 게시물이 없거나 너무 적습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    // Recommend 정보를 RecommendPostDto 형태로 변환
    List<RecommendPostDto> result = new ArrayList<>();
    for (Recommend recommend : recommends) {
      Long postId = recommend.getPost().getId();
      Category category = recommend.getCategory();

      result.add(getRecommendPostDto(member, postId, locale, travelType, category));
    }

    return result;
  }

  public List<RecommendPostDto> getRandomRecommendedPosts(MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    Language locale = memberInfoDto.getLanguage();

    // 이색체험 제외하고 모두 조회
    List<Recommend> recommends = recommendRepository.findAllWithoutExperience();
    if (recommends == null || recommends.size() < 2) {
      String errorMessage = "추천 게시물이 없거나 너무 적습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    // Recommend 정보를 RecommendPostDto 형태로 변환
    List<RecommendPostDto> result = new ArrayList<>();
    for (Recommend recommend : recommends) {
      Long postId = recommend.getPost().getId();
      Category category = recommend.getCategory();
      TravelType travelType = recommend.getTravelType();

      result.add(getRecommendPostDto(member, postId, locale, travelType, category));
    }

    return result;
  }

  private RecommendPostDto getRecommendPostDto(Member member, Long postId, Language locale,
      TravelType travelType, Category category) {

    RecommendPostDto recommendPostDto = switch (category) {
      case NATURE -> recommendRepository.findNatureRecommendPostDto(postId, locale, travelType);

      case FESTIVAL -> recommendRepository.findFestivalRecommendPostDto(postId, locale, travelType);

      case EXPERIENCE ->
          recommendRepository.findExperienceRecommendPostDto(postId, locale, travelType);

      case MARKET -> recommendRepository.findMarketRecommendPostDto(postId, locale, travelType);

      case NANA -> recommendRepository.findNanaRecommendPostDto(postId, locale, travelType);

      default -> throw new NotFoundException("해당 추천 게시물 정보가 존재하지 않습니다.");
    };

    if (recommendPostDto == null) {
      String errorMessage = postId + ", " + category.name() + "게시물이 없습니다.";
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

  private TravelType getRandomTravelType() {
    Random random = new Random();
    List<TravelType> values = new ArrayList<>(List.of(TravelType.values()));
    values.remove(TravelType.NONE);

    return values.get(random.nextInt(values.size()));
  }

  // 타입 별 추천 게시물 개수가 많아질 경우에 사용
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
