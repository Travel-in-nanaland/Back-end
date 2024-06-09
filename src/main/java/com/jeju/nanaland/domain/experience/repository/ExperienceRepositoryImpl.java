package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceCompositeDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ExperienceRepositoryImpl implements ExperienceRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public ExperienceCompositeDto findCompositeDtoById(Long id, Locale locale) {
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
            experienceTrans.addressTag,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.eq(id).and(experienceTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, locale);

    List<ExperienceCompositeDto> resultDto = queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experience.ratingAvg,
            experienceTrans.language.locale,
            experienceTrans.title,
            experienceTrans.content,
            experienceTrans.address,
            experienceTrans.addressTag,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .where(experienceTrans.title.contains(keyword)
            .or(experienceTrans.addressTag.contains(keyword))
            .or(experienceTrans.content.contains(keyword))
            .or(experience.id.in(idListContainAllHashtags)))
        .orderBy(experienceTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(experience.count())
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.locale.eq(locale))
        .where(experienceTrans.title.contains(keyword)
            .or(experienceTrans.addressTag.contains(keyword))
            .or(experienceTrans.content.contains(keyword))
            .or(experience.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(experience.id)
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.postId.eq(experience.id)
            .and(hashtag.category.content.eq(CategoryContent.EXPERIENCE))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(experience.id)
        .having(experience.id.count().eq(splitKeyword(keyword).stream().count()))
        .fetch();
  }

  private List<String> splitKeyword(String keyword) {
    String[] tokens = keyword.split("\\s+");
    List<String> tokenList = new ArrayList<>();

    for (String token : tokens) {
      tokenList.add(token.trim());
    }
    return tokenList;
  }
}
