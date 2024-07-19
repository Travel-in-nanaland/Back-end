package com.jeju.nanaland.domain.review.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
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
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
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

    // 해당 리뷰의 좋아요 개수
    JPQLQuery<Long> heartCountQuery = JPAExpressions
        .select(reviewHeart.count())
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id));

    // 현재 로그인한 회원이 해당 리뷰에 좋아요를 했는지
    BooleanExpression isReviewHeartQuery = JPAExpressions.selectOne()
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id)
            .and(reviewHeart.member.id.eq(memberId)))
        .exists();

    List<ReviewDetailDto> resultDto = queryFactory
        .select(new QReviewResponse_ReviewDetailDto(
                review.id,
                member.id,
                member.nickname,
                new QImageFileDto(
                    imageFile.originUrl,
                    imageFile.thumbnailUrl),
                ExpressionUtils.as(memberReviewCountQuery, "memberReviewCount"),
                review.rating,
                review.content,
                review.createdAt,
                ExpressionUtils.as(heartCountQuery, "heartCount"),
                ExpressionUtils.as(isReviewHeartQuery, "isReviewHeart")
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

  @Override
  public Page<MemberReviewDetailDto> findReviewListByMember(Member member, Language language,
      Pageable pageable) {

    // 해당 리뷰의 좋아요 개수
    JPQLQuery<Long> heartCountQuery = JPAExpressions
        .select(reviewHeart.count())
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id));

    // 1. 좋아요 개수를 포함한 리뷰 세부 정보 조회
    List<MemberReviewDetailDto> resultDto = queryFactory
        .select(new QReviewResponse_MemberReviewDetailDto(
                review.id,
                review.post.id,
                review.category,
                review.rating,
                review.content,
                review.createdAt,
                ExpressionUtils.as(heartCountQuery, "heartCount")
            )
        )
        .from(review)
        .where(review.member.id.eq(member.getId()))
        .orderBy(review.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 2. EXPERIENCE 카테고리의 리뷰에 대한 제목 조회
    Map<Long, String> experienceTitleMap = fetchExperienceTitles(resultDto, language);

    // TODO: 3. 맛집 추가하기

    List<Long> reviewIds = resultDto.stream().map(MemberReviewDetailDto::getId).toList();

    // 4. 각 리뷰별 이미지 리스트 조회
    Map<Long, List<ImageFileDto>> reviewImagesMap = queryFactory
        .selectFrom(reviewImageFile)
        .innerJoin(reviewImageFile.imageFile, imageFile)
        .where(reviewImageFile.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewImageFile.review.id)
            .as(GroupBy.list(new QImageFileDto(
                imageFile.originUrl,
                imageFile.thumbnailUrl)
            )));

    // 5. 각 리뷰별 키워드 리스트 조회
    Map<Long, Set<ReviewTypeKeyword>> reviewTypeKeywordMap = queryFactory
        .selectFrom(reviewKeyword)
        .where(reviewKeyword.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewKeyword.review.id)
            .as(GroupBy.set(reviewKeyword.reviewTypeKeyword)));

    // 추가 정보를 리뷰 DTO에 설정
    resultDto.forEach(
        reviewDetailDto -> {
          // 제목 설정
          if (reviewDetailDto.getCategory() == Category.EXPERIENCE) {
            reviewDetailDto.setPlaceName(
                experienceTitleMap.getOrDefault(reviewDetailDto.getPostId(), "")
            );
          }
          // TODO: 맛집 추가하기

          // 이미지 설정
          reviewDetailDto.setImages(
              reviewImagesMap.getOrDefault(reviewDetailDto.getId(), Collections.emptyList()));

          // 키워드 설정
          reviewDetailDto.setReviewTypeKeywords(
              reviewTypeKeywordMap.getOrDefault(reviewDetailDto.getId(), Collections.emptySet())
                  .stream()
                  .map(reviewTypeKeyword ->
                      reviewTypeKeyword.getValueByLocale(language)).collect(Collectors.toSet()));
        }
    );

    // 5. 페이지네이션을 위한 전체 리뷰 개수 조회
    JPAQuery<Long> countQuery = queryFactory
        .select(review.countDistinct())
        .from(review)
        .where(review.member.id.eq(member.getId()));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  // 리뷰 이미지 파일 조회
  private Map<Long, ImageFileDto> fetchReviewImages(List<Long> reviewIds) {
    return queryFactory
        .selectFrom(reviewImageFile)
        .innerJoin(reviewImageFile.imageFile, imageFile)
        .where(reviewImageFile.review.id.in(reviewIds))
        .limit(1)
        .transform(GroupBy.groupBy(reviewImageFile.review.id)
            .as(new QImageFileDto(
                imageFile.originUrl,
                imageFile.thumbnailUrl
            )));
  }

  // EXPERIENCE 카테고리의 리뷰 제목 조회
  private Map<Long, String> fetchExperienceTitles(List<MemberReviewDetailDto> reviews,
      Language language) {
    List<Long> experienceIds = reviews.stream()
        .filter(dto -> dto.getCategory() == Category.EXPERIENCE)
        .map(MemberReviewDetailDto::getPostId)
        .toList();

    return queryFactory
        .select(experienceTrans.title)
        .from(experience)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.in(experienceIds)
            .and(experienceTrans.language.eq(language)))
        .transform(GroupBy.groupBy(experience.id)
            .as(experienceTrans.title));
  }
}
