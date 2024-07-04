package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;
import static com.jeju.nanaland.domain.member.entity.QRecommend.recommend;
import static com.jeju.nanaland.domain.member.entity.QRecommendTrans.recommendTrans;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.dto.QMemberResponse_RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecommendRepositoryImpl implements RecommendRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public RecommendPostDto findNatureRecommendPostDto(Long postId, Locale locale,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.postId,
            recommend.category.content,
            imageFile.originUrl,
            natureTrans.title,
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.locale.eq(locale)))
        .innerJoin(recommend.imageFile, imageFile)
        .innerJoin(nature).on(nature.id.eq(recommend.postId))
        .innerJoin(nature.natureTrans, natureTrans).on(natureTrans.language.locale.eq(locale))
        .where(recommend.postId.eq(postId)
            .and(recommend.category.content.eq(CategoryContent.NATURE))
            .and(recommend.memberTravelType.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public RecommendPostDto findExperienceRecommendPostDto(Long postId, Locale locale,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.postId,
            recommend.category.content,
            imageFile.originUrl,
            experienceTrans.title,
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.locale.eq(locale)))
        .innerJoin(recommend.imageFile, imageFile)
        .innerJoin(experience).on(experience.id.eq(recommend.postId))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .where(recommend.postId.eq(postId)
            .and(recommend.category.content.eq(CategoryContent.EXPERIENCE))
            .and(recommend.memberTravelType.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public RecommendPostDto findMarketRecommendPostDto(Long postId, Locale locale,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.postId,
            recommend.category.content,
            imageFile.originUrl,
            marketTrans.title,
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.locale.eq(locale)))
        .innerJoin(recommend.imageFile, imageFile)
        .innerJoin(market).on(market.id.eq(recommend.postId))
        .innerJoin(market.marketTrans, marketTrans).on(marketTrans.language.locale.eq(locale))
        .where(recommend.postId.eq(postId)
            .and(recommend.category.content.eq(CategoryContent.MARKET))
            .and(recommend.memberTravelType.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public RecommendPostDto findFestivalRecommendPostDto(Long postId, Locale locale,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.postId,
            recommend.category.content,
            imageFile.originUrl,
            festivalTrans.title,
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.locale.eq(locale)))
        .innerJoin(recommend.imageFile, imageFile)
        .innerJoin(festival).on(festival.id.eq(recommend.postId))
        .innerJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .where(recommend.postId.eq(postId)
            .and(recommend.category.content.eq(CategoryContent.FESTIVAL))
            .and(recommend.memberTravelType.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public RecommendPostDto findNanaRecommendPostDto(Long postId, Locale locale,
      TravelType travelType) {
    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.postId,
            recommend.category.content,
            imageFile.originUrl,
            nanaTitle.heading,
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.locale.eq(locale)))
        .innerJoin(recommend.imageFile, imageFile)
        .innerJoin(nana)
        .on(nana.id.eq(recommend.postId))
        .innerJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana).and(nanaTitle.language.locale.eq(locale)))
        .where(recommend.postId.eq(postId)
            .and(recommend.category.content.eq(CategoryContent.NANA))
            .and(recommend.memberTravelType.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public List<Recommend> findAllWithoutExperience() {
    return queryFactory
        .select(recommend)
        .from(recommend)
        .where(recommend.category.content.ne(CategoryContent.EXPERIENCE))
        .fetch();
  }
}
