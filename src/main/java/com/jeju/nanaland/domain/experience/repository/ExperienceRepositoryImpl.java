package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;

import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceCompositeDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExperienceRepositoryImpl implements ExperienceRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public ExperienceCompositeDto findExperienceCompositeDto(Long id,
      String locale) {
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
}
