package com.jeju.nanaland.domain.stay.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.stay.entity.QStay.stay;
import static com.jeju.nanaland.domain.stay.entity.QStayTrans.stayTrans;

import com.jeju.nanaland.domain.stay.dto.QStayCompositeDto;
import com.jeju.nanaland.domain.stay.dto.StayCompositeDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class StayRepositoryImpl implements StayRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public StayCompositeDto findCompositeDtoById(Long id, String locale) {
    return queryFactory
        .select(new QStayCompositeDto(
            stay.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            stay.price,
            stay.contact,
            stay.homepage,
            stay.parking,
            stay.ratingAvg,
            language.locale,
            stayTrans.title,
            stayTrans.intro,
            stayTrans.address,
            stayTrans.time))
        .from(stay)
        .from(stay)
        .leftJoin(stay.imageFile, imageFile)
        .leftJoin(stay.stayTrans, stayTrans)
        .where(stay.id.eq(id).and(stayTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<StayCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable) {

    List<StayCompositeDto> result = queryFactory
        .select(new QStayCompositeDto(
            stay.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            stay.price,
            stay.contact,
            stay.homepage,
            stay.parking,
            stay.ratingAvg,
            language.locale,
            stayTrans.title,
            stayTrans.intro,
            stayTrans.address,
            stayTrans.time))
        .from(stay)
        .leftJoin(stay.imageFile, imageFile)
        .leftJoin(stay.stayTrans, stayTrans)
        .where(stayTrans.title.like(title)
            .and(stayTrans.language.locale.eq(locale)))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(stay.count())
        .from(stay)
        .leftJoin(stay.imageFile, imageFile)
        .leftJoin(stay.stayTrans, stayTrans)
        .where(stayTrans.title.like(title)
            .and(stayTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
  }
}
