package com.jeju.nanaland.domain.favorite.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
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
import com.jeju.nanaland.domain.favorite.entity.Favorite;
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
  public Page<ThumbnailDto> findNatureThumbnails(Long memberId, Locale locale, Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nature.id,
            natureTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(nature).on(favorite.postId.eq(nature.id))
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(natureTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.NATURE)
            .and(language.locale.eq(locale)))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .join(nature).on(favorite.postId.eq(nature.id))
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(natureTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.NATURE)
            .and(language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findNatureThumbnailByPostId(Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nature.id,
            natureTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(nature).on(favorite.postId.eq(nature.id))
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(natureTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.NATURE)
            .and(favorite.postId.eq(postId))
            .and(language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findExperienceThumbnails(Long memberId, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            experience.id,
            experienceTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(experience).on(favorite.postId.eq(experience.id))
        .leftJoin(experience.experienceTrans, experienceTrans)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experienceTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.EXPERIENCE)
            .and(language.locale.eq(locale)))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .join(experience).on(favorite.postId.eq(experience.id))
        .leftJoin(experience.experienceTrans, experienceTrans)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experienceTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.EXPERIENCE)
            .and(language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findExperienceThumbnailByPostId(Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            experience.id,
            experienceTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(experience).on(favorite.postId.eq(experience.id))
        .leftJoin(experience.experienceTrans, experienceTrans)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experienceTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.EXPERIENCE)
            .and(favorite.postId.eq(postId))
            .and(language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findFestivalThumbnails(Long memberId, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            festival.id,
            festivalTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(festival).on(favorite.postId.eq(festival.id))
        .leftJoin(festival.festivalTrans, festivalTrans)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festivalTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.FESTIVAL)
            .and(language.locale.eq(locale)))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .join(festival).on(favorite.postId.eq(festival.id))
        .leftJoin(festival.festivalTrans, festivalTrans)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festivalTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.FESTIVAL)
            .and(language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findFestivalThumbnailByPostId(Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            festival.id,
            festivalTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(festival).on(favorite.postId.eq(festival.id))
        .leftJoin(festival.festivalTrans, festivalTrans)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festivalTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.FESTIVAL)
            .and(favorite.postId.eq(postId))
            .and(language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findMarketThumbnails(Long memberId, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            market.id,
            marketTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(market).on(favorite.postId.eq(market.id))
        .leftJoin(market.marketTrans, marketTrans)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(marketTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.MARKET)
            .and(language.locale.eq(locale)))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .join(market).on(favorite.postId.eq(market.id))
        .leftJoin(market.marketTrans, marketTrans)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(marketTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.MARKET)
            .and(language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findMarketThumbnailByPostId(Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            market.id,
            marketTrans.title,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(market).on(favorite.postId.eq(market.id))
        .leftJoin(market.marketTrans, marketTrans)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(marketTrans.language, language)
        .where(favorite.category.content.eq(CategoryContent.MARKET)
            .and(favorite.postId.eq(postId))
            .and(language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ThumbnailDto> findNanaThumbnails(Long memberId, Locale locale,
      Pageable pageable) {
    List<ThumbnailDto> resultDto = queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nana.id,
            nanaTitle.heading,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(nana).on(favorite.postId.eq(nana.id))
        .join(nanaTitle).on(nana.eq(nanaTitle.nana))
        .leftJoin(nanaTitle.imageFile, imageFile)
        .leftJoin(nanaTitle.language, language)
        .where(favorite.category.content.eq(CategoryContent.NANA)
            .and(language.locale.eq(locale)))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .join(nana).on(favorite.postId.eq(nana.id))
        .join(nanaTitle).on(nana.eq(nanaTitle.nana))
        .leftJoin(nanaTitle.imageFile, imageFile)
        .leftJoin(nanaTitle.language, language)
        .where(favorite.category.content.eq(CategoryContent.NANA)
            .and(language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public ThumbnailDto findNanaThumbnailByPostId(Long postId, Locale locale) {
    return queryFactory
        .select(new QFavoriteResponse_ThumbnailDto(
            nana.id,
            nanaTitle.heading,
            imageFile.thumbnailUrl
        ))
        .from(favorite)
        .join(nana).on(favorite.postId.eq(nana.id))
        .join(nanaTitle).on(nana.eq(nanaTitle.nana))
        .leftJoin(nanaTitle.imageFile, imageFile)
        .leftJoin(nanaTitle.language, language)
        .where(favorite.category.content.eq(CategoryContent.NANA)
            .and(favorite.postId.eq(postId))
            .and(language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<Favorite> findAllCategoryFavorite(Member member, Pageable pageable) {
    List<Favorite> result = queryFactory
        .selectFrom(favorite)
        .where(favorite.member.eq(member))
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .where(favorite.member.eq(member))
        .orderBy(favorite.createdAt.desc());

    return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
  }
}
