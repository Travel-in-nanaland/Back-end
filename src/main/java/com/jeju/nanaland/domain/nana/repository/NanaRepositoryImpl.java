package com.jeju.nanaland.domain.nana.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_ThumbnailDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NanaRepositoryImpl implements NanaRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<NanaResponse.ThumbnailDto> findThumbnailDto(Locale locale) {
    return queryFactory.select(new QNanaResponse_ThumbnailDto(
            nana.id,
            imageFile.thumbnailUrl
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nanaTitle.imageFile, imageFile)
        .where(nana.active.eq(true).and(nanaTitle.language.locale.eq(locale)))
        .fetch();
  }
}
