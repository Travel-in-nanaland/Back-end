package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.QNatureCompositeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NatureRepositoryImpl implements NatureRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public NatureCompositeDto findCompositeDtoById(Long id, Locale locale) {
    return queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(id)
            .and(natureTrans.language.locale.eq(locale))
        )
        .fetchOne();
  }

  @Override
  public Page<NatureCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {
    List<NatureCompositeDto> resultDto = queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.locale.eq(locale)
            .and(natureTrans.title.contains(keyword)
                .or(natureTrans.addressTag.contains(keyword))
                .or(natureTrans.content.contains(keyword))))
        .orderBy(natureTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.locale.eq(locale)
            .and(natureTrans.title.contains(keyword)
                .or(natureTrans.addressTag.contains(keyword))
                .or(natureTrans.content.contains(keyword))));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<NatureCompositeDto> findNatureThumbnails(Locale locale,
      List<String> addressFilterList, Pageable pageable) {
    List<NatureCompositeDto> resultDto = queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.imageFile, imageFile)
        .where(natureTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList)))
        .orderBy(nature.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return natureTrans.addressTag.in(addressFilterList);
    }
  }
}
