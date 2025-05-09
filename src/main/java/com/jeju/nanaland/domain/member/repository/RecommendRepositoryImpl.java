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

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.QMemberResponse_RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecommendRepositoryImpl implements RecommendRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public MemberResponse.RecommendPostDto findNatureRecommendPostDto(Long postId, Language language,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.post.id,
            recommend.category.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            recommendTrans.title.coalesce(natureTrans.title),
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(imageFile)
        .on(recommend.firstImageFile.eq(imageFile))
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.eq(language)))
        .innerJoin(nature)
        .on(nature.id.eq(recommend.post.id))
        .innerJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.eq(language))
        .where(recommend.post.id.eq(postId)
            .and(recommend.category.eq(Category.NATURE))
            .and(recommend.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public MemberResponse.RecommendPostDto findExperienceRecommendPostDto(Long postId,
      Language language,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.post.id,
            Expressions.asString("")
                .append(new CaseBuilder()
                    .when(experience.experienceType.stringValue().eq("ACTIVITY"))
                    .then("ACTIVITY")
                    .otherwise("CULTURE_AND_ARTS")),
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            recommendTrans.title.coalesce(experienceTrans.title),
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(imageFile)
        .on(recommend.firstImageFile.eq(imageFile))
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.eq(language)))
        .innerJoin(experience)
        .on(experience.id.eq(recommend.post.id))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.eq(language))
        .where(recommend.post.id.eq(postId)
            .and(recommend.category.eq(Category.EXPERIENCE))
            .and(recommend.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public MemberResponse.RecommendPostDto findMarketRecommendPostDto(Long postId, Language language,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.post.id,
            recommend.category.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            recommendTrans.title.coalesce(marketTrans.title),
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(imageFile)
        .on(recommend.firstImageFile.eq(imageFile))
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.eq(language)))
        .innerJoin(market)
        .on(market.id.eq(recommend.post.id))
        .innerJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.eq(language))
        .where(recommend.post.id.eq(postId)
            .and(recommend.category.eq(Category.MARKET))
            .and(recommend.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public MemberResponse.RecommendPostDto findFestivalRecommendPostDto(Long postId,
      Language language,
      TravelType travelType) {

    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.post.id,
            recommend.category.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            recommendTrans.title.coalesce(festivalTrans.title),
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(imageFile)
        .on(recommend.firstImageFile.eq(imageFile))
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.eq(language)))
        .innerJoin(festival)
        .on(festival.id.eq(recommend.post.id))
        .innerJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.eq(language))
        .where(recommend.post.id.eq(postId)
            .and(recommend.category.eq(Category.FESTIVAL))
            .and(recommend.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public MemberResponse.RecommendPostDto findNanaRecommendPostDto(Long postId, Language language,
      TravelType travelType) {
    return queryFactory
        .select(new QMemberResponse_RecommendPostDto(
            recommend.post.id,
            recommend.category.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            recommendTrans.title.coalesce(nanaTitle.heading),
            recommendTrans.introduction
        ))
        .from(recommend)
        .innerJoin(imageFile)
        .on(recommend.firstImageFile.eq(imageFile))
        .innerJoin(recommendTrans)
        .on(recommendTrans.recommend.eq(recommend)
            .and(recommendTrans.language.eq(language)))
        .innerJoin(nana)
        .on(nana.id.eq(recommend.post.id))
        .innerJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana)
            .and(nanaTitle.language.eq(language)))
        .where(recommend.post.id.eq(postId)
            .and(recommend.category.eq(Category.NANA))
            .and(recommend.travelType.eq(travelType)))
        .fetchOne();
  }

  @Override
  public List<Long> findAllIds() {
    return queryFactory
        .select(recommend.id)
        .from(recommend)
        .fetch();
  }
}
