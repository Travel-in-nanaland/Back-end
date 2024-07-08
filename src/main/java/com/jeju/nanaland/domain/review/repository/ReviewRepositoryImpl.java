package com.jeju.nanaland.domain.review.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.member.entity.QMember.member;
import static com.jeju.nanaland.domain.review.entity.QReview.review;
import static com.jeju.nanaland.domain.review.entity.QReviewHeart.reviewHeart;
import static com.jeju.nanaland.domain.review.entity.QReviewImageFile.reviewImageFile;
import static com.jeju.nanaland.domain.review.entity.QReviewKeyword.reviewKeyword;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.QImageFileDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ReviewDetailDto> findReviewListByPostId(MemberInfoDto memberInfoDto,
      Category category, Long id, Pageable pageable) {
    Long memberId = memberInfoDto.getMember().getId();
    Language language = memberInfoDto.getLanguage();

    // 리뷰 작성자의 총 작성한 리뷰 개수
    JPQLQuery<Long> memberReviewCountQuery = JPAExpressions
        .select(review.count())
        .from(review)
        .where(review.member.id.eq(member.id));

    // 리뷰 작성자의 총 리뷰 평균 점수
    JPQLQuery<Double> memberReviewRatingAvgQuery = JPAExpressions
        .select(review.rating.avg())
        .from(review)
        .where(review.member.id.eq(member.id));

    // 해당 리뷰의 좋아요 개수
    JPQLQuery<Long> heartCountQuery = JPAExpressions
        .select(reviewHeart.count())
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id));

    // 현재 로그인한 회원이 해당 리뷰에 좋아요를 했는지
    BooleanExpression isFavoriteQuery = JPAExpressions.selectOne()
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id)
            .and(reviewHeart.member.id.eq(memberId)))
        .exists();

    List<ReviewDetailDto> resultDto = queryFactory
        .select(new QReviewResponse_ReviewDetailDto(
                review.id,
                member.nickname,
                new QImageFileDto(
                    imageFile.originUrl,
                    imageFile.thumbnailUrl),
                ExpressionUtils.as(memberReviewCountQuery, "memberReviewCount"),
                ExpressionUtils.as(memberReviewRatingAvgQuery, "memberReviewAvgRating"),
                review.content,
                review.createdAt,
                ExpressionUtils.as(heartCountQuery, "heartCount"),
                ExpressionUtils.as(isFavoriteQuery, "isFavorite")
            )
        )
        .from(review)
        .innerJoin(review.member, member)
        .innerJoin(member.profileImageFile, imageFile)
        .where(review.category.eq(category)
            .and(review.post.id.eq(id)))
        .orderBy(review.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<Long> reviewIds = resultDto.stream().map(ReviewDetailDto::getId).toList();

    // 각 리뷰별 이미지 리스트 조회
    Map<Long, List<ImageFileDto>> reviewImagesMap = queryFactory
        .selectFrom(reviewImageFile)
        .innerJoin(reviewImageFile.imageFile, imageFile)
        .where(reviewImageFile.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewImageFile.review.id)
            .as(GroupBy.list(new QImageFileDto(
                imageFile.originUrl,
                imageFile.thumbnailUrl)
            )));

    // 각 리뷰별 키워드 리스트 조회
    Map<Long, Set<ReviewTypeKeyword>> reviewTypeKeywordMap = queryFactory
        .selectFrom(reviewKeyword)
        .where(reviewKeyword.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewKeyword.review.id)
            .as(GroupBy.set(reviewKeyword.reviewTypeKeyword)));

    // resultDto에 담아주기
    resultDto.forEach(
        reviewDetailDto -> {
          reviewDetailDto.setImages(
              reviewImagesMap.getOrDefault(reviewDetailDto.getId(), Collections.emptyList()));

          reviewDetailDto.setReviewTypeKeywords(
              reviewTypeKeywordMap.getOrDefault(reviewDetailDto.getId(), Collections.emptySet())
                  .stream()
                  .map(reviewTypeKeyword ->
                      reviewTypeKeyword.getValueByLocale(language)).collect(Collectors.toSet()));
        }
    );

    // 리뷰 리스트의 총 개수
    JPAQuery<Long> countQuery = queryFactory
        .select(review.countDistinct())
        .from(review)
        .innerJoin(review.member, member)
        .innerJoin(member.profileImageFile, imageFile)
        .where(review.category.eq(category)
            .and(review.post.id.eq(id)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);

  }

  @Override
  public Double findTotalRatingAvg(Category category, Long id) {
    Double avgRating = queryFactory
        .select(review.rating.avg())
        .from(review)
        .where(review.category.eq(category)
            .and(review.post.id.eq(id)))
        .fetchOne();
    return avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0;
  }
}
