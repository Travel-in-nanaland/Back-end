package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.QNatureCompositeDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NatureRepositoryImpl implements NatureRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public NatureCompositeDto findNatureCompositeDto(Long id, Locale locale) {
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
            natureTrans.amenity
        ))
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(id).and(natureTrans.language.locale.eq(locale)))
        .fetchOne();
  }
}
