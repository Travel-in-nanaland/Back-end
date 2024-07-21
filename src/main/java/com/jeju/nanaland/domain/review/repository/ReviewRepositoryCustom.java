package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {

  Page<ReviewDetailDto> findReviewListByPostId(MemberInfoDto memberInfoDto, Category category,
      Long id, Pageable pageable);

  Double findTotalRatingAvg(Category category, Long id);

  MyReviewDetailDto findExperienceMyReviewDetail(Long reviewId, MemberInfoDto memberInfoDto);

  // TODO 맛집 생기면 주석 지우기
//  MyReviewDetailDto findRestaurantMyReviewDetail(Long reviewId, MemberInfoDto memberInfoDto);
}
