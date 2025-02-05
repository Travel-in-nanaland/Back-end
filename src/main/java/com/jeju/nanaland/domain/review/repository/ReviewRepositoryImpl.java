package com.jeju.nanaland.domain.review.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.member.entity.QMember.member;
import static com.jeju.nanaland.domain.report.entity.claim.QClaimReport.claimReport;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurant.restaurant;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantTrans.restaurantTrans;
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
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportType;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ExperienceInfo;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
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

  private static final int PREVIEW_LIMIT = 12;
  private final JPAQueryFactory queryFactory;

  // 해당 리뷰를 신고한 적이 없는 경우에 true
  private static BooleanExpression getReviewReportNotExists(Long memberId) {
    return JPAExpressions.selectOne()
        .from(claimReport)
        .where(claimReport.member.id.eq(memberId)
            .and(claimReport.referenceId.eq(review.id))
            .and(claimReport.claimReportType.eq(ClaimReportType.REVIEW)))
        .notExists();
  }

  // 해당 유저를 신고한 적이 없는 경우에 true
  private static BooleanExpression getMemberReportNotExists(Long currentMemberId) {
    return JPAExpressions.selectOne()
        .from(claimReport)
        .where(claimReport.member.id.eq(currentMemberId)
            .and(claimReport.referenceId.eq(review.member.id))
            .and(claimReport.claimReportType.eq(ClaimReportType.MEMBER)))
        .notExists();
  }

  @Override
  public Page<ReviewDetailDto> findReviewListByPostId(MemberInfoDto memberInfoDto,
      Category category, Long id, Pageable pageable) {
    Long memberId = memberInfoDto.getMember().getId();
    Language language = memberInfoDto.getLanguage();

    List<ReviewDetailDto> resultDto = queryFactory
        .select(new QReviewResponse_ReviewDetailDto(
                review.id,
                member.id,
                member.nickname,
                new QImageFileDto(
                    imageFile.originUrl,
                    imageFile.thumbnailUrl),
                // 리뷰 작성자의 총 작성한 리뷰 개수
                ExpressionUtils.as(getMemberReviewCountQuery(), "memberReviewCount"),
                review.rating,
                review.content,
                review.createdAt,
                // 해당 리뷰의 좋아요 개수
                ExpressionUtils.as(getHeartCountQuery(), "heartCount"),
                // 현재 로그인한 회원이 해당 리뷰에 좋아요를 했는지
                ExpressionUtils.as(getIsReviewHeartQuery(memberId), "isReviewHeart")
            )
        )
        .from(review)
        .innerJoin(review.member, member)
        .innerJoin(member.profileImageFile, imageFile)
        .where(review.category.eq(category)
            .and(review.post.id.eq(id))
            .and(getReviewReportNotExists(memberId)) // 해당 리뷰를 신고한 적이 있는지 조회
            .and(getMemberReportNotExists(memberId))) // 해당 유저를 신고한 적이 있는지 조회
        .orderBy(review.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<Long> reviewIds = resultDto.stream().map(ReviewDetailDto::getId).toList();

    // 각 리뷰별 이미지 리스트 조회
    Map<Long, List<ImageFileDto>> reviewImagesMap = getReviewImagesMap(
        reviewIds);

    // 각 리뷰별 키워드 리스트 조회
    Map<Long, Set<ReviewTypeKeyword>> reviewTypeKeywordMap = getReviewTypeKeywordMap(
        reviewIds);

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

          reviewDetailDto.setMyReview(reviewDetailDto.getMemberId().equals(memberId));
        }
    );

    // 리뷰 리스트의 총 개수
    JPAQuery<Long> countQuery = queryFactory
        .select(review.countDistinct())
        .from(review)
        .innerJoin(review.member, member)
        .innerJoin(member.profileImageFile, imageFile)
        .where(review.category.eq(category)
            .and(review.post.id.eq(id))
            .and(getReviewReportNotExists(memberId))
            .and(getMemberReportNotExists(memberId)));

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
  public MyReviewDetailDto findExperienceMyReviewDetail(Long reviewId,
      MemberInfoDto memberInfoDto) {
    return queryFactory
        .select(
            new QReviewResponse_MyReviewDetailDto(review.id, experience.firstImageFile.originUrl,
                experience.firstImageFile.thumbnailUrl, experienceTrans.title,
                experienceTrans.address, review.rating,
                review.content))
        .from(review)
        .leftJoin(experience)
        .on(review.post.id.eq(experience.id))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.language.eq(memberInfoDto.getLanguage()).and(review.id.eq(reviewId)))
        .fetchOne();
  }

  @Override
  public MyReviewDetailDto findRestaurantMyReviewDetail(Long reviewId,
      MemberInfoDto memberInfoDto) {
    return queryFactory
        .select(
            new QReviewResponse_MyReviewDetailDto(review.id, restaurant.firstImageFile.originUrl,
                restaurant.firstImageFile.thumbnailUrl, restaurantTrans.title,
                restaurantTrans.address, review.rating,
                review.content))
        .from(review)
        .leftJoin(restaurant)
        .on(review.post.id.eq(restaurant.id))
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .where(restaurantTrans.language.eq(memberInfoDto.getLanguage()).and(review.id.eq(reviewId)))
        .fetchOne();
  }

  @Override
  public Page<MemberReviewDetailDto> findReviewListByMember(Long currentMemberId, Member member,
      Language language, Pageable pageable) {

    // 1. 좋아요 개수를 포함한 리뷰 세부 정보 조회
    JPAQuery<MemberReviewDetailDto> query = queryFactory
        .select(new QReviewResponse_MemberReviewDetailDto(
                review.id,
                review.post.id,
                review.category.stringValue(),
                review.rating,
                review.content,
                review.createdAt,
                // 해당 리뷰의 좋아요 개수
                ExpressionUtils.as(getHeartCountQuery(), "heartCount"),
                // 현재 로그인한 회원이 해당 리뷰에 좋아요를 했는지
                ExpressionUtils.as(getIsReviewHeartQuery(currentMemberId), "isReviewHeart")
            )
        )
        .from(review)
        .where(review.member.id.eq(member.getId()));

    // 해당 리뷰를 신고한 적이 있거나 해당 유저를 신고한 적이 있는지
    if (!currentMemberId.equals(member.getId())) {
      query.where(getReviewReportNotExists(currentMemberId)
          .and(getMemberReportNotExists(currentMemberId)));
    }

    List<MemberReviewDetailDto> resultDto = query
        .orderBy(review.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 2. EXPERIENCE 카테고리의 리뷰에 대한 제목 조회
    List<Long> experienceIds = resultDto.stream()
        .filter(dto -> dto.getCategory().equals(Category.EXPERIENCE.name()))
        .map(MemberReviewDetailDto::getPostId)
        .toList();
    Map<Long, ExperienceInfo> experienceTitleAndTypeMap = fetchExperienceTitlesAndTypes(experienceIds, language);

    // 3. RESTAURANT 카테고리의 리뷰에 대한 제목 조회
    List<Long> restaurantIds = resultDto.stream()
        .filter(dto -> dto.getCategory().equals(Category.RESTAURANT.name()))
        .map(MemberReviewDetailDto::getPostId)
        .toList();
    Map<Long, String> restaurantTitleMap = fetchRestaurantTitles(restaurantIds, language);

    List<Long> reviewIds = resultDto.stream().map(MemberReviewDetailDto::getId).toList();

    // 4. 각 리뷰별 이미지 리스트 조회
    Map<Long, List<ImageFileDto>> reviewImagesMap = getReviewImagesMap(
        reviewIds);

    // 5. 각 리뷰별 키워드 리스트 조회
    Map<Long, Set<ReviewTypeKeyword>> reviewTypeKeywordMap = getReviewTypeKeywordMap(
        reviewIds);

    // 추가 정보를 리뷰 DTO에 설정
    resultDto.forEach(
        reviewDetailDto -> {
          String category = reviewDetailDto.getCategory();
          // 제목 설정
          if (category.equals(Category.EXPERIENCE.name())) {
            ExperienceInfo experienceInfo = experienceTitleAndTypeMap.get(reviewDetailDto.getPostId());
            reviewDetailDto.setPlaceName(experienceInfo.getTitle());
            reviewDetailDto.setCategory(experienceInfo.getExperienceType());
          }

          else if (category.equals(Category.RESTAURANT.name())) {
            reviewDetailDto.setPlaceName(
                restaurantTitleMap.getOrDefault(reviewDetailDto.getPostId(), "")
            );
          }

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

    if (!currentMemberId.equals(member.getId())) {
      countQuery.where(getReviewReportNotExists(currentMemberId)
          .and(getMemberReportNotExists(currentMemberId)));
    }

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public List<MemberReviewPreviewDetailDto> findReviewPreviewByMember(Long currentMemberId,
      Member member, Language language) {

    JPAQuery<MemberReviewPreviewDetailDto> query = queryFactory
        .select(new QReviewResponse_MemberReviewPreviewDetailDto(
                review.id,
                review.post.id,
                review.category.stringValue(),
                review.createdAt,
                // 해당 리뷰의 좋아요 개수
                ExpressionUtils.as(getHeartCountQuery(), "heartCount")
            )
        )
        .from(review)
        .where(review.member.id.eq(member.getId()));

    // 해당 리뷰를 신고한 적이 있거나 해당 유저를 신고한 적이 있는지
    if (!currentMemberId.equals(member.getId())) {
      query.where(getReviewReportNotExists(currentMemberId)
          .and(getMemberReportNotExists(currentMemberId)));
    }

    List<MemberReviewPreviewDetailDto> resultDto = query
        .orderBy(review.createdAt.desc())
        .limit(PREVIEW_LIMIT)
        .fetch();

    // 2. EXPERIENCE 카테고리의 리뷰에 대한 제목 조회
    List<Long> experienceIds = resultDto.stream()
        .filter(dto -> dto.getCategory().equals(Category.EXPERIENCE.name()))
        .map(MemberReviewPreviewDetailDto::getPostId)
        .toList();
    Map<Long, ExperienceInfo> experienceTitleAndTypeMap = fetchExperienceTitlesAndTypes(experienceIds, language);

    // 3. RESTAURANT 카테고리의 리뷰에 대한 제목 조회
    List<Long> restaurantIds = resultDto.stream()
        .filter(dto -> dto.getCategory().equals(Category.RESTAURANT.name()))
        .map(MemberReviewPreviewDetailDto::getPostId)
        .toList();
    Map<Long, String> restaurantTitleMap = fetchRestaurantTitles(restaurantIds, language);

    // 4. 각 리뷰에 대한 이미지 파일 정보 조회
    List<Long> reviewIds = resultDto.stream().map(MemberReviewPreviewDetailDto::getId).toList();
    Map<Long, ImageFileDto> reviewImageMap = fetchReviewImages(reviewIds);

    // 추가 정보를 리뷰 DTO에 설정
    resultDto.forEach(
        reviewDetailDto -> {
          // 이미지 설정
          reviewDetailDto.setImageFileDto(
              reviewImageMap.getOrDefault(reviewDetailDto.getId(), null));

          // 제목 설정
          if (reviewDetailDto.getCategory().equals(Category.EXPERIENCE.name())) {
            ExperienceInfo experienceInfo = experienceTitleAndTypeMap.get(reviewDetailDto.getPostId());
            reviewDetailDto.setPlaceName(experienceInfo.getTitle());
            reviewDetailDto.setCategory(experienceInfo.getExperienceType());
          }

          if (reviewDetailDto.getCategory().equals(Category.RESTAURANT.name())) {
            reviewDetailDto.setPlaceName(
                restaurantTitleMap.getOrDefault(reviewDetailDto.getPostId(), "")
            );
          }
        }
    );
    return resultDto;
  }

  @Override
  public Long findTotalCountByMember(Long currentMemberId, Member member) {
    JPAQuery<Long> query = queryFactory
        .select(review.countDistinct())
        .from(review)
        .where(review.member.id.eq(member.getId()));

    // 해당 리뷰를 신고한 적이 있거나 해당 유저를 신고한 적이 있는지
    if (!currentMemberId.equals(member.getId())) {
      query.where(getReviewReportNotExists(currentMemberId)
          .and(getMemberReportNotExists(currentMemberId)));
    }
    return query.fetchOne();
  }

  // 리뷰 작성자의 총 작성한 리뷰 개수
  private JPQLQuery<Long> getMemberReviewCountQuery() {
    return JPAExpressions
        .select(review.count())
        .from(review)
        .where(review.member.id.eq(member.id));
  }

  // 해당 리뷰의 좋아요 개수
  private JPQLQuery<Long> getHeartCountQuery() {
    return JPAExpressions
        .select(reviewHeart.count())
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id));
  }

  // 현재 로그인한 회원이 해당 리뷰에 좋아요를 했는지
  private BooleanExpression getIsReviewHeartQuery(Long memberId) {
    return JPAExpressions.selectOne()
        .from(reviewHeart)
        .where(reviewHeart.review.id.eq(review.id)
            .and(reviewHeart.member.id.eq(memberId)))
        .exists();
  }

  // 각 리뷰별 키워드 리스트 조회
  private Map<Long, Set<ReviewTypeKeyword>> getReviewTypeKeywordMap(List<Long> reviewIds) {
    return queryFactory
        .selectFrom(reviewKeyword)
        .where(reviewKeyword.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewKeyword.review.id)
            .as(GroupBy.set(reviewKeyword.reviewTypeKeyword)));
  }

  // 리뷰 별 이미지 리스트 조회
  private Map<Long, List<ImageFileDto>> getReviewImagesMap(List<Long> reviewIds) {
    return queryFactory
        .selectFrom(reviewImageFile)
        .innerJoin(reviewImageFile.imageFile, imageFile)
        .where(reviewImageFile.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewImageFile.review.id)
            .as(GroupBy.list(new QImageFileDto(
                imageFile.originUrl,
                imageFile.thumbnailUrl)
            )));
  }

  // 리뷰 이미지 파일 조회
  private Map<Long, ImageFileDto> fetchReviewImages(List<Long> reviewIds) {
    return queryFactory
        .selectFrom(reviewImageFile)
        .innerJoin(reviewImageFile.imageFile, imageFile)
        .where(reviewImageFile.review.id.in(reviewIds))
        .transform(GroupBy.groupBy(reviewImageFile.review.id)
            .as(new QImageFileDto(
                imageFile.originUrl,
                imageFile.thumbnailUrl
            )));
  }

  // EXPERIENCE 카테고리의 리뷰 제목 조회
  private Map<Long, ExperienceInfo> fetchExperienceTitlesAndTypes(List<Long> experienceIds,
      Language language) {

    return queryFactory
        .select(Projections.constructor(ExperienceInfo.class,
            experienceTrans.title,
            experience.experienceType.stringValue()))
        .from(experience)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.in(experienceIds)
            .and(experienceTrans.language.eq(language)))
        .transform(GroupBy.groupBy(experience.id)
            .as(Projections.constructor(ExperienceInfo.class,
                experienceTrans.title,
                experience.experienceType.stringValue())));
  }

  // RESTAURANT 카테고리의 리뷰 제목 조회
  private Map<Long, String> fetchRestaurantTitles(List<Long> restaurantIds,
      Language language) {

    return queryFactory
        .select(restaurantTrans.title)
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .where(restaurant.id.in(restaurantIds)
            .and(restaurantTrans.language.eq(language)))
        .transform(GroupBy.groupBy(restaurant.id)
            .as(restaurantTrans.title));
  }
}
