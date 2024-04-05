package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;

import com.jeju.nanaland.domain.festival.dto.FestivalFestivalTransDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalFestivalTransDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FestivalRepositoryImpl implements FestivalRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public FestivalFestivalTransDto getFestivalFestivalTransDtoByIdAndLocale(Long id, String locale) {
    return queryFactory
        .select(new QFestivalFestivalTransDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            language.locale,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.time,
            festivalTrans.fee
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.locale.eq(locale)))
        .fetchOne();
  }
}
