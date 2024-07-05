package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.List;

public interface RecommendRepositoryCustom {

  RecommendPostDto findNatureRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findNatureRecommendResultPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findExperienceRecommendPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findExperienceRecommendResultPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findMarketRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findMarketRecommendResultPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findFestivalRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findFestivalRecommendResultPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findNanaRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findNanaRecommendResultPostDto(Long postId, Locale locale,
      TravelType travelType);

  List<Recommend> findAllWithoutExperience();
}
