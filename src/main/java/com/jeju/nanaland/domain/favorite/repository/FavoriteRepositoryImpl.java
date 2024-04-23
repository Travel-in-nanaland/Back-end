package com.jeju.nanaland.domain.favorite.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.favorite.entity.QFavorite.favorite;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.dto.QFavoriteResponse_ThumbnailDto;
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
        .orderBy(favorite.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }
}
