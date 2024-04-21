package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.QNatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.QNatureResponse_NatureThumbnail;
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
  public Page<NatureCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
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
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.title.contains(title)
            .and(natureTrans.language.locale.eq(locale)))
        .orderBy(natureTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.title.contains(title)
            .and(natureTrans.language.locale.eq(locale))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<NatureThumbnail> findNatureThumbnails(Locale locale, String addressFilter,
      Pageable pageable) {
    List<NatureThumbnail> resultDto = queryFactory
        .select(new QNatureResponse_NatureThumbnail(
            nature.id,
            natureTrans.title,
            imageFile.thumbnailUrl,
            natureTrans.address
        ))
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.imageFile, imageFile)
        .where(natureTrans.language.locale.eq(locale)
            .and(natureTrans.address.contains(addressFilter)))
        .orderBy(nature.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.locale.eq(locale)
            .and(natureTrans.address.contains(addressFilter)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }
}
