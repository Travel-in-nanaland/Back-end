package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.QExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceResponse_ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.querydsl.core.types.dsl.BooleanExpression;
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
  public ExperienceCompositeDto findCompositeDtoById(Long id, Language language) {
    return queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experienceTrans.language,
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
        .where(experience.id.eq(id).and(experienceTrans.language.eq(language)))
        .fetchOne();
  }

  @Override
  public Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String keyword, Language language,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, language);

    List<ExperienceCompositeDto> resultDto = queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experienceTrans.language,
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
        .on(experienceTrans.language.eq(language))
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
        .on(experienceTrans.language.eq(language))
        .where(experienceTrans.title.contains(keyword)
            .or(experienceTrans.addressTag.contains(keyword))
            .or(experienceTrans.content.contains(keyword))
            .or(experience.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<ExperienceThumbnail> findExperienceThumbnails(Language language,
      ExperienceType experienceType, List<String> keywordFilterList, List<String> addressFilterList,
      Pageable pageable) {

    List<ExperienceThumbnail> resultDto = queryFactory
        .select(new QExperienceResponse_ExperienceThumbnail(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experienceTrans.title,
            experienceTrans.addressTag
        ))
        .from(experience)
        .innerJoin(experience.firstImageFile, imageFile)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.language.eq(language)
            .and(experience.experienceType.eq(experienceType))  // 이색체험 타입(액티비티/문화예술)
            .and(addressTagCondition(addressFilterList))  // 지역필터
            .and(keywordCondition(keywordFilterList)))  // 키워드 필터
        .orderBy(experience.priority.desc(),  // 우선순위 정렬
            experience.createdAt.desc())  // 최신순 정렬
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(experience.count())
        .from(experience)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.language.eq(language)
            .and(addressTagCondition(addressFilterList))
            .and(keywordCondition(keywordFilterList)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(experience.id)
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(experience.id)
            .and(hashtag.category.eq(Category.EXPERIENCE))
            .and(hashtag.language.eq(language)))
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

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return experienceTrans.addressTag.in(addressFilterList);
    }
  }

  private BooleanExpression keywordCondition(List<String> keywordFilterList) {
    if (keywordFilterList.isEmpty()) {
      return null;
    } else {
      BooleanExpression result = null;
      // keywordFilterList 의 요소 중 하나라도 포함하면 True
      for (String keyword : keywordFilterList) {
        BooleanExpression isContains = experience.keywords.contains(keyword);
        if (result == null) {
          result = isContains;
        } else {
          result = result.or(isContains);
        }
      }

      return result;
    }
  }
}
