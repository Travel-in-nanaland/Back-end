package com.jeju.nanaland.domain.experience.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPost.post;
import static com.jeju.nanaland.domain.experience.entity.QExperience.experience;
import static com.jeju.nanaland.domain.experience.entity.QExperienceKeyword.experienceKeyword;
import static com.jeju.nanaland.domain.experience.entity.QExperienceTrans.experienceTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceSearchDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.QExperienceResponse_ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.QExperienceSearchDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_SearchPostForReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ExperienceRepositoryImpl implements ExperienceRepositoryCustom {

  private static final Logger log = LoggerFactory.getLogger(ExperienceRepositoryImpl.class);
  private final JPAQueryFactory queryFactory;

  @Override
  public ExperienceCompositeDto findCompositeDtoById(Long id, Language language) {
    return queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experience.homepage,
            experienceTrans.language,
            experienceTrans.title,
            experienceTrans.content,
            experienceTrans.address,
            experienceTrans.addressTag,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity,
            experienceTrans.fee
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.eq(id).and(experienceTrans.language.eq(language)))
        .fetchOne();
  }

  @Override
  public ExperienceCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id,
      Language language) {
    return queryFactory
        .select(new QExperienceCompositeDto(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experience.contact,
            experience.homepage,
            experienceTrans.language,
            experienceTrans.title,
            experienceTrans.content,
            experienceTrans.address,
            experienceTrans.addressTag,
            experienceTrans.intro,
            experienceTrans.details,
            experienceTrans.time,
            experienceTrans.amenity,
            experienceTrans.fee
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .where(experience.id.eq(id).and(experienceTrans.language.eq(language)))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  @Override
  public Page<ExperienceSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      Language language, Pageable pageable) {

    // experience_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(experience.id, experience.id.count())
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(experience.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(experience.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(experience.id),  // key: experience_id
            tuple -> tuple.get(experience.id.count())  // value: 매칭된 키워드 개수
        ));

    List<ExperienceSearchDto> resultDto = queryFactory
        .select(new QExperienceSearchDto(
            experience.id,
            experienceTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            experience.createdAt
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (ExperienceSearchDto experienceSearchDto : resultDto) {
      Long id = experienceSearchDto.getId();
      experienceSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(experienceSearchDto -> experienceSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<ExperienceSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(ExperienceSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(ExperienceSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<ExperienceSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  @Override
  public Page<ExperienceSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language, Pageable pageable) {

    // experience_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(experience.id, experience.id.count())
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(experience.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(experience.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(experience.id),  // key: experience_id
            tuple -> tuple.get(experience.id.count())  // value: 매칭된 키워드 개수
        ));

    List<ExperienceSearchDto> resultDto = queryFactory
        .select(new QExperienceSearchDto(
            experience.id,
            experienceTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            experience.createdAt
        ))
        .from(experience)
        .leftJoin(experience.firstImageFile, imageFile)
        .leftJoin(experience.experienceTrans, experienceTrans)
        .on(experienceTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (ExperienceSearchDto experienceSearchDto : resultDto) {
      Long id = experienceSearchDto.getId();
      experienceSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(experienceSearchDto -> experienceSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<ExperienceSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(ExperienceSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<ExperienceSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  @Override
  public Page<ExperienceThumbnail> findExperienceThumbnails(Language language,
      ExperienceType experienceType, List<ExperienceTypeKeyword> keywordFilterList,
      List<AddressTag> addressTags, Pageable pageable) {

    List<ExperienceThumbnail> resultDto = queryFactory
        .selectDistinct(new QExperienceResponse_ExperienceThumbnail(
            experience.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            experienceTrans.title,
            experienceTrans.addressTag
        ))
        .from(experience)
        .innerJoin(experience.firstImageFile, imageFile)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experienceKeyword)
        .on(experienceKeyword.experience.id.eq(experience.id))
        .where(experienceTrans.language.eq(language)
            .and(experience.experienceType.eq(experienceType))  // 이색체험 타입(액티비티/문화예술)
            .and(addressTagCondition(language, addressTags))  // 지역필터
            .and(keywordCondition(keywordFilterList)))  // 키워드 필터
        .orderBy(experience.priority.desc(),  // 우선순위 정렬
            experience.createdAt.desc())  // 최신순 정렬
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .selectDistinct(experience.countDistinct())
        .from(experience)
        .innerJoin(experience.firstImageFile, imageFile)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experienceKeyword)
        .on(experienceKeyword.experience.id.eq(experience.id))
        .where(experienceTrans.language.eq(language)
            .and(experience.experienceType.eq(experienceType))
            .and(addressTagCondition(language, addressTags))
            .and(keywordCondition(keywordFilterList)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Set<ExperienceTypeKeyword> getExperienceTypeKeywordSet(Long postId) {
    Map<Long, Set<ExperienceTypeKeyword>> map = queryFactory
        .selectFrom(experienceKeyword)
        .where(experienceKeyword.experience.id.eq(postId))
        .transform(GroupBy.groupBy(experienceKeyword.experience.id)
            .as(GroupBy.set(experienceKeyword.experienceTypeKeyword)));

    return map.getOrDefault(postId, Collections.emptySet());
  }

  @Override
  public Set<ExperienceTypeKeyword> getExperienceTypeKeywordSetWithWithPessimisticLock(
      Long postId) {
    Map<Long, Set<ExperienceTypeKeyword>> map = queryFactory
        .selectFrom(experienceKeyword)
        .where(experienceKeyword.experience.id.eq(postId))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 비관적 락 추가
        .transform(GroupBy.groupBy(experienceKeyword.experience.id)
            .as(GroupBy.set(experienceKeyword.experienceTypeKeyword)));

    return map.getOrDefault(postId, Collections.emptySet());
  }

  @Override
  public List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language) {
    return queryFactory
        .select(
            new QReviewResponse_SearchPostForReviewDto(experience.id,
                Expressions.constant(Category.EXPERIENCE.name()),
                experienceTrans.title, experience.firstImageFile, experienceTrans.address))
        .from(experience)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .where(experienceTrans.language.eq(language))
        .fetch();
  }

  @Override
  public List<Long> findAllIds() {
    return queryFactory
        .select(experience.id)
        .from(experience)
        .fetch();
  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            experience.id,
            experienceTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(experience)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experience.firstImageFile, imageFile)
        .where(
            experience.id.eq(postId),
            experienceTrans.language.eq(language))
        .fetchOne();
  }


  @Override
  public List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(experience.id, experienceTrans.title,
                experienceTrans.addressTag,
                Expressions.constant(Category.EXPERIENCE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(experience, post)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experience.firstImageFile, imageFile)
        .where(
            experience.id.eq(post.id),
            experienceTrans.language.eq(language),
            post.viewCount.gt(0))
        .orderBy(post.viewCount.desc())
        .limit(3)
        .fetch();
  }

  @Override
  public PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(experience.id, experienceTrans.title,
                experienceTrans.addressTag,
                Expressions.constant(Category.EXPERIENCE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(experience, post)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experience.firstImageFile, imageFile)
        .where(
            experience.id.eq(post.id),
            experience.id.notIn(excludeIds),
            experienceTrans.language.eq(language)
        )
        .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
        .fetchFirst();
  }

  @Override
  public PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(experience.id, experienceTrans.title,
                experienceTrans.addressTag,
                Expressions.constant(Category.EXPERIENCE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(experience, post)
        .innerJoin(experience.experienceTrans, experienceTrans)
        .innerJoin(experience.firstImageFile, imageFile)
        .where(
            experience.id.eq(post.id),
            experience.id.eq(postId),
            experienceTrans.language.eq(language)
        )
        .fetchOne();
  }

  private List<Long> getIdListContainAllHashtags(String keywords, Language language) {
    return queryFactory
        .select(experience.id)
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(experience.id)
            .and(hashtag.category.eq(Category.EXPERIENCE))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(experience.id)
        .having(experience.id.count().eq(splitKeyword(keywords).stream().count()))
        .fetch();
  }

  private List<Long> getIdListContainAllHashtags(List<String> keywords, Language language) {
    return queryFactory
        .select(experience.id)
        .from(experience)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(experience.id)
            .and(hashtag.category.eq(Category.EXPERIENCE))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(experience.id)
        .having(experience.id.count().eq(keywords.stream().count()))
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

  private BooleanExpression addressTagCondition(Language language, List<AddressTag> addressTags) {
    if (addressTags.isEmpty()) {
      return null;
    } else {
      List<String> addressTagFilters = addressTags.stream()
          .map(address -> address.getValueByLocale(language)).toList();
      return experienceTrans.addressTag.in(addressTagFilters);
    }
  }

  private BooleanExpression keywordCondition(List<ExperienceTypeKeyword> keywordFilterList) {
    if (keywordFilterList.isEmpty()) {
      return null;
    } else {
      return experienceKeyword.experienceTypeKeyword.in(keywordFilterList);
    }
  }

  private Expression<Long> countMatchingWithKeyword(List<String> keywords) {
    return Expressions.asNumber(0L)
        .add(countMatchingConditionWithKeyword(experienceTrans.title.toLowerCase().trim(), keywords,
            0))
        .add(countMatchingConditionWithKeyword(experienceTrans.addressTag.toLowerCase().trim(),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(experienceTrans.content, keywords, 0));
  }

  private Expression<Integer> countMatchingConditionWithKeyword(StringExpression condition,
      List<String> keywords, int idx) {
    if (idx == keywords.size()) {
      return Expressions.asNumber(0);
    }

    return new CaseBuilder()
        .when(condition.contains(keywords.get(idx)))
        .then(1)
        .otherwise(0)
        .add(countMatchingConditionWithKeyword(condition, keywords, idx + 1));
  }

  private BooleanExpression containsAllKeywords(StringExpression condition, List<String> keywords) {
    BooleanExpression expression = null;
    for (String keyword : keywords) {
      BooleanExpression containsKeyword = condition.contains(keyword);
      expression = (expression == null) ? containsKeyword : expression.and(containsKeyword);
    }
    return expression;
  }
}