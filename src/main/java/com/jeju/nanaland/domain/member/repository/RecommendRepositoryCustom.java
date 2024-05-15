package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;

public interface RecommendRepositoryCustom {

  RecommendPostDto findNatureRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findExperienceRecommendPostDto(Long postId, Locale locale,
      TravelType travelType);

  RecommendPostDto findMarketRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findFestivalRecommendPostDto(Long postId, Locale locale, TravelType travelType);

  RecommendPostDto findNanaRecommendPostDto(Long postId, Locale locale, TravelType travelType);

}
