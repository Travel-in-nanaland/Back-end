package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberTypeService {

  private static final Random RANDOM = new Random();
  private final RecommendRepository recommendRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final RestaurantRepository restaurantRepository;
  private final ExperienceRepository experienceRepository;

  // 유저 타입 갱신
  @Transactional
  public void updateMemberType(MemberInfoDto memberInfoDto,
      MemberRequest.UpdateTypeDto updateTypeDto) {

    Member member = memberInfoDto.getMember();
    String newTravelType = updateTypeDto.getType();

    member.updateTravelType(TravelType.valueOf(newTravelType));
  }

  // 추천 게시물 2개 반환
  public List<MemberResponse.RecommendPostDto> getRecommendPostsByType(
      MemberInfoDto memberInfoDto) {

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
    List<MemberResponse.RecommendPostDto> result = new ArrayList<>();
    for (Recommend recommend : recommends) {
      Long postId = recommend.getPost().getId();
      Category category = recommend.getCategory();

      result.add(getRecommendPostDto(member, postId, locale, travelType, category));
    }

    return result;
  }

  // 랜덤 추천 게시물 3개 반환
  public List<MemberResponse.RecommendPostDto> getRandomRecommendedPosts(
      MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    Language language = memberInfoDto.getLanguage();

    // 랜덤으로 추천 게시물 3개 조회
    int totalCount = 0;
    List<Long> recommendIds = recommendRepository.findAllIds();
    List<Long> experienceIds = experienceRepository.findAllIds();
    List<Long> restaurantIds = restaurantRepository.findAllIds();
    totalCount += recommendIds.size();
    totalCount += experienceIds.size();
    totalCount += restaurantIds.size();

    Set<Integer> randomIdxs = new HashSet<>();
    while (randomIdxs.size() < 3) {
      randomIdxs.add(RANDOM.nextInt(totalCount));
    }

    List<MemberResponse.RecommendPostDto> result = new ArrayList<>();
    for (Integer randomIdx : randomIdxs) {
      // 랜덤 게시물이 recommend 에 있는 경우
      if (randomIdx < recommendIds.size()) {
        Recommend recommend = recommendRepository.findById(recommendIds.get(randomIdx))
            .orElseThrow(() -> new NotFoundException("해당 추천 게시물이 존재하지 않습니다."));

        Long postId = recommend.getPost().getId();
        TravelType travelType = recommend.getTravelType();
        Category category = recommend.getCategory();
        result.add(getRecommendPostDto(member, postId, language, travelType, category));
      }
      // 랜덤 게시물이 이색체험인 경우
      else if (randomIdx < recommendIds.size() + experienceIds.size()) {
        randomIdx -= recommendIds.size();
        Long postId = experienceIds.get(randomIdx);
        result.add(getExperiencePostDto(member, postId, language));
      }
      // 랜덤 게시물이 맛집인 경우
      else {
        randomIdx -= recommendIds.size();
        randomIdx -= experienceIds.size();
        Long postId = restaurantIds.get(randomIdx);
        result.add(getRestaurantPostDto(member, postId, language));
      }
    }

    return result;
  }

  // RecommendPostDto 반환
  private MemberResponse.RecommendPostDto getRecommendPostDto(Member member, Long postId,
      Language locale, TravelType travelType, Category category) {

    MemberResponse.RecommendPostDto recommendPostDto = switch (category) {
      case NATURE -> recommendRepository.findNatureRecommendPostDto(postId, locale, travelType);

      case FESTIVAL -> recommendRepository.findFestivalRecommendPostDto(postId, locale, travelType);

      case EXPERIENCE ->
          recommendRepository.findExperienceRecommendPostDto(postId, locale, travelType);

      case MARKET -> recommendRepository.findMarketRecommendPostDto(postId, locale, travelType);

      case NANA -> recommendRepository.findNanaRecommendPostDto(postId, locale, travelType);

      default -> throw new NotFoundException(CATEGORY_NOT_FOUND.getMessage());
    };

    if (recommendPostDto == null) {
      String errorMessage = postId + ", " + category.name() + "게시물이 없습니다.";
      log.error(errorMessage);
      throw new NotFoundException(errorMessage);
    }

    // 좋아요 여부 체크
    recommendPostDto.setFavorite(memberFavoriteService.isPostInFavorite(member, category, postId));

    return recommendPostDto;
  }

  private MemberResponse.RecommendPostDto getExperiencePostDto(Member member, Long postId,
      Language language) {
    ExperienceCompositeDto compositeDto = experienceRepository.findCompositeDtoById(postId,
        language);
    MemberResponse.RecommendPostDto recommendPostDto = MemberResponse.RecommendPostDto.builder()
        .id(compositeDto.getId())
        .category(compositeDto.getExperienceType().name())
        .title(compositeDto.getTitle())
        .firstImage(compositeDto.getFirstImage())
        .build();

    // 좋아요 여부 체크
    recommendPostDto.setFavorite(
        memberFavoriteService.isPostInFavorite(member, Category.EXPERIENCE, postId));

    return recommendPostDto;
  }

  private MemberResponse.RecommendPostDto getRestaurantPostDto(Member member, Long postId,
      Language language) {
    RestaurantCompositeDto compositeDto = restaurantRepository.findCompositeDtoById(postId,
        language);
    MemberResponse.RecommendPostDto recommendPostDto = MemberResponse.RecommendPostDto.builder()
        .id(compositeDto.getId())
        .category(Category.RESTAURANT.name())
        .title(compositeDto.getTitle())
        .firstImage(compositeDto.getFirstImage())
        .build();

    // 좋아요 여부 체크
    recommendPostDto.setFavorite(
        memberFavoriteService.isPostInFavorite(member, Category.RESTAURANT, postId));

    return recommendPostDto;
  }

  // TravelType 랜덤
  private TravelType getRandomTravelType() {
    List<TravelType> values = new ArrayList<>(List.of(TravelType.values()));
    values.remove(TravelType.NONE);

    return values.get(RANDOM.nextInt(values.size()));
  }

  // 타입 별 추천 게시물 개수가 많아질 경우에 사용
  private List<Recommend> getRandomTwoPosts(List<Recommend> recommends) {
    if (recommends.size() == 2) {
      return recommends;
    }

    int randomIdx;
    List<Recommend> result = new ArrayList<>();
    randomIdx = RANDOM.nextInt(recommends.size());
    result.add(recommends.get(randomIdx));
    recommends.remove(randomIdx);

    randomIdx = RANDOM.nextInt(recommends.size());
    result.add(recommends.get(randomIdx));

    return result;
  }
}
