package com.jeju.nanaland.domain.favorite.repository;

import static com.jeju.nanaland.domain.common.entity.QCategory.category;
import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.favorite.entity.QFavorite.favorite;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.dto.QFavoriteResponse_ThumbnailDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ThumbnailDto> findNatureThumbnails(Member member, Locale locale, Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nature.id,
            natureTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nature)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nature.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NATURE)))
        .innerJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.locale.eq(locale))
        .innerJoin(nature.firstImageFile, imageFile)
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nature.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NATURE)))
        .innerJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.locale.eq(locale))
        .innerJoin(nature.firstImageFile, imageFile);

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findNatureThumbnailByPostId(Member member, Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nature.id,
            natureTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nature)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nature.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NATURE)))
        .innerJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.locale.eq(locale))
        .innerJoin(nature.firstImageFile, imageFile)
        .where(favorite.post.id.eq(postId))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findExperienceThumbnails(Member member, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            experience.id,
            experienceTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(experience)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(experience.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.EXPERIENCE)))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .innerJoin(experience.firstImageFile, imageFile)
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(experience.count())
        .from(experience)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(experience.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.EXPERIENCE)))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .innerJoin(experience.firstImageFile, imageFile);

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findExperienceThumbnailByPostId(Member member, Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            experience.id,
            experienceTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(experience)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(experience.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.EXPERIENCE)))
        .innerJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .innerJoin(experience.firstImageFile, imageFile)
        .where(favorite.post.id.eq(postId))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findFestivalThumbnails(Member member, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            festival.id,
            festivalTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(festival)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(festival.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.FESTIVAL)))
        .innerJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .innerJoin(festival.firstImageFile, imageFile)
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(festival.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.FESTIVAL)))
        .innerJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .innerJoin(festival.firstImageFile, imageFile);

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findFestivalThumbnailByPostId(Member member, Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            festival.id,
            festivalTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(festival)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(festival.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.FESTIVAL)))
        .innerJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .innerJoin(festival.firstImageFile, imageFile)
        .where(favorite.post.id.eq(postId))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findMarketThumbnails(Member member, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            market.id,
            marketTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(market)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(market.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.MARKET)))
        .innerJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.locale.eq(locale))
        .innerJoin(market.firstImageFile, imageFile)
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(market.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.MARKET)))
        .innerJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.locale.eq(locale))
        .innerJoin(market.firstImageFile, imageFile);

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findMarketThumbnailByPostId(Member member, Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            market.id,
            marketTrans.title,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(market)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(market.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.MARKET)))
        .innerJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.locale.eq(locale))
        .innerJoin(market.firstImageFile, imageFile)
        .where(favorite.post.id.eq(postId))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findNanaThumbnails(Member member, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nana.id,
            nanaTitle.heading,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nana)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nana.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NANA)))
        .innerJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana)
            .and(nanaTitle.language.locale.eq(locale)))
        .innerJoin(nana.nanaTitleImageFile, imageFile)
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nana.count())
        .from(nana)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nana.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NANA)))
        .innerJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana).and(nanaTitle.language.locale.eq(locale)))
        .innerJoin(nana.nanaTitleImageFile, imageFile);

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findNanaThumbnailByPostId(Member member, Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nana.id,
            nanaTitle.heading,
            category.content.stringValue(),
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nana)
        .innerJoin(favorite)
        .on(favorite.post.id.eq(nana.id)
            .and(favorite.member.eq(member))
            .and(favorite.category.content.eq(CategoryContent.NANA)))
        .innerJoin(nanaTitle)
        .on(nana.eq(nanaTitle.nana)
            .and(nanaTitle.language.locale.eq(locale)))
        .innerJoin(nana.nanaTitleImageFile, imageFile)
        .where(favorite.post.id.eq(postId))
        .fetchOne();
  }
}
