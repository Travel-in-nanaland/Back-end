package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.List;

public interface RecommendRepositoryCustom {

  RecommendPostDto findNatureRecommendPostDto(Long postId, Language locale, TravelType travelType);

  RecommendPostDto findExperienceRecommendPostDto(Long postId, Language locale,
      TravelType travelType);

  RecommendPostDto findMarketRecommendPostDto(Long postId, Language locale, TravelType travelType);

  RecommendPostDto findFestivalRecommendPostDto(Long postId, Language locale,
      TravelType travelType);

  RecommendPostDto findNanaRecommendPostDto(Long postId, Language locale, TravelType travelType);

  List<Long> findAllIds();
}
