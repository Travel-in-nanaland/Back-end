package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;

import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceCompositeDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ExperienceRepositoryImpl implements ExperienceRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public ExperienceCompositeDto findCompositeDtoById(Long id, String locale) {
    return queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experience.ratingAvg,
            language.locale,
            experienceTrans.title,
            experienceTrans.content,
            experienceTrans.address,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity
        ))
        .from(experience)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.eq(id).and(experienceTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ExperienceCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable) {
    List<ExperienceCompositeDto> ResultDto = queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experience.ratingAvg,
            language.locale,
            experienceTrans.title,
            experienceTrans.content,
            experienceTrans.address,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity
        ))
        .from(experience)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.title.contains(title)
            .and(experienceTrans.language.locale.eq(locale)))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(experience.count())
        .from(experience)
        .leftJoin(experience.imageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.title.contains(title)
            .and(experienceTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(ResultDto, pageable, countQuery::fetchOne);
  }
}
