package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {

  Page<ReviewDetailDto> findReviewListByPostId(MemberInfoDto memberInfoDto, Category category,
      Long id, Pageable pageable);

  Double findTotalRatingAvg(Category category, Long id);

  Page<MemberReviewDetailDto> findReviewListByMember(Long currentMemberId, Member member,
      Language language, Pageable pageable);

  List<MemberReviewPreviewDetailDto> findReviewPreviewByMember(Long currentMemberId, Member member,
      Language language);

  Long findTotalCountByMember(Long currentMemberId, Member member);

  MyReviewDetailDto findExperienceMyReviewDetail(Long reviewId, MemberInfoDto memberInfoDto);

  MyReviewDetailDto findRestaurantMyReviewDetail(Long reviewId, MemberInfoDto memberInfoDto);
}
