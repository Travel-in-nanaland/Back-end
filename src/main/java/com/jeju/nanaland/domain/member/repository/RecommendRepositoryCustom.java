package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.List;

public interface RecommendRepositoryCustom {

  MemberResponse.RecommendPostDto findNatureRecommendPostDto(Long postId, Language locale, TravelType travelType);

  MemberResponse.RecommendPostDto findExperienceRecommendPostDto(Long postId, Language locale,
      TravelType travelType);

  MemberResponse.RecommendPostDto findMarketRecommendPostDto(Long postId, Language locale, TravelType travelType);

  MemberResponse.RecommendPostDto findFestivalRecommendPostDto(Long postId, Language locale,
      TravelType travelType);

  MemberResponse.RecommendPostDto findNanaRecommendPostDto(Long postId, Language locale, TravelType travelType);

  List<Long> findAllIds();
}
