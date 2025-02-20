package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPost.post;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalSearchDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalSearchDto;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class FestivalRepositoryImpl implements FestivalRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public FestivalCompositeDto findCompositeDtoById(Long id, Language language) {
    return queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.onGoing,
            festival.homepage,
            festivalTrans.language,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        )
        .fetchOne();
  }

  @Override
  public FestivalCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language language) {
    return queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.onGoing,
            festival.homepage,
            festivalTrans.language,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        )
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  /**
   * 게시물의 제목, 주소태그, 키워드, 해시태그 중 하나라도 겹치는 게시물이 있다면 조회 일치한 수, 생성일자 내림차순
   *
   * @param keywords 유저 키워드 리스트
   * @param language 유저 언어
   * @param pageable 페이징
   * @return 검색결과
   */
  @Override
  public Page<FestivalSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      List<AddressTag> addressTags, LocalDate startDate, LocalDate endDate,
      Language language, Pageable pageable) {
    // festival_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(festival.id, festival.id.count())
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(festival.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(festival.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(festival.id),  // key: festival_id
            tuple -> tuple.get(festival.id.count())  // value: 매칭된 키워드 개수
        ));

    List<FestivalSearchDto> resultDto = queryFactory
        .select(new QFestivalSearchDto(
            festival.id,
            festivalTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            festival.createdAt
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.eq(language))
        .where(
            // 기간필터
            periodCondition(startDate, endDate)
            // 지역필터
            , addressTagCondition(language, addressTags))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (FestivalSearchDto festivalSearchDto : resultDto) {
      Long id = festivalSearchDto.getId();
      festivalSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(festivalSearchDto -> festivalSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<FestivalSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(FestivalSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(FestivalSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<FestivalSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  /**
   * 게시물의 제목, 주소태그, 키워드, 해시태그와 모두 겹치는 게시물이 있다면 조회 생성일자 내림차순
   *
   * @param keywords 유저 키워드 리스트
   * @param language 유저 언어
   * @param pageable 페이징
   * @return 검색결과
   */
  @Override
  public Page<FestivalSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      List<AddressTag> addressTags, LocalDate startDate, LocalDate endDate,
      Language language, Pageable pageable) {
    // festival_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(festival.id, festival.id.count())
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(festival.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(festival.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(festival.id),  // key: festival_id
            tuple -> tuple.get(festival.id.count())  // value: 매칭된 키워드 개수
        ));

    List<FestivalSearchDto> resultDto = queryFactory
        .select(new QFestivalSearchDto(
            festival.id,
            festivalTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            festival.createdAt
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.eq(language))
        .where(
            // 기간필터
            periodCondition(startDate, endDate)
            // 지역필터
            , addressTagCondition(language, addressTags))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (FestivalSearchDto festivalSearchDto : resultDto) {
      Long id = festivalSearchDto.getId();
      festivalSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(festivalSearchDto -> festivalSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<FestivalSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(FestivalSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<FestivalSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(festival.id)
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(festival.id)
            .and(hashtag.category.eq(Category.FESTIVAL))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword))
            .and(festival.status.eq(Status.ACTIVE)))
        .groupBy(festival.id)
        .having(festival.id.count().eq(splitKeyword(keyword).stream().count()))
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

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Language language,
      Pageable pageable, boolean onGoing, List<AddressTag> addressTags) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.onGoing,
            festival.homepage,
            festivalTrans.language,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
            .and(festivalTrans.language.eq(language))
            .and(addressTagCondition(language, addressTags))
        )
        .orderBy(festival.endDate.desc()) // 최근에 끝난 순
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
            .and(addressTagCondition(language, addressTags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoBySeason(Language language, Pageable pageable,
      String season) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.onGoing,
            festival.homepage,
            festivalTrans.language,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.season.like("%" + season + "%")
            .and(festivalTrans.language.eq(language))
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE)))
        .orderBy(festival.endDate.asc())// 종료일 오름차 순 (곧 종료되는)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.season.like("%" + season + "%")
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByMonth(Language language, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<AddressTag> addressTags) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.onGoing,
            festival.homepage,
            festivalTrans.language,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where( // 축제의 시작일이 필터 시작일 보다 작고 축제의 종료일은 필터의 시작 일보다 클 때
            ((festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
                // 축제의 기간이 필터 사이에 있을 대
                .or((festival.startDate.goe(startDate)
                    .and(festival.endDate.loe(endDate))))
                // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
                .or((festival.startDate.loe(endDate))
                    .and(festival.endDate.goe(endDate))))

                .and(festivalTrans.language.eq(language)
                    .and(addressTagCondition(language, addressTags)))
                .and(festival.status.eq(Status.ACTIVE))
        )
        .orderBy(festival.endDate.asc()) // 종료일 오름차 순 (곧 종료되는)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where( // 축제의 시작일이 필터 시작일 보다 작고 축제의 종료일은 필터의 시작 일보다 클 때
            ((festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
                // 축제의 기간이 필터 사이에 있을 대
                .or((festival.startDate.goe(startDate)
                    .and(festival.endDate.loe(endDate))))
                // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
                .or((festival.startDate.loe(endDate))
                    .and(festival.endDate.goe(endDate))))

                .and(festivalTrans.language.eq(language)
                    .and(addressTagCondition(language, addressTags)))

                .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            festival.id,
            festivalTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(festival)
        .innerJoin(festival.festivalTrans, festivalTrans)
        .innerJoin(festival.firstImageFile, imageFile)
        .where(
            festival.id.eq(postId),
            festivalTrans.language.eq(language))
        .fetchOne();
  }

  @Override
  public List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(festival.id, festivalTrans.title,
                festivalTrans.addressTag,
                Expressions.constant(Category.FESTIVAL.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(festival, post)
        .innerJoin(festival.festivalTrans, festivalTrans)
        .innerJoin(festival.firstImageFile, imageFile)
        .where(
            festival.id.eq(post.id),
            festivalTrans.language.eq(language),
            festival.id.notIn(excludeIds),
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
            new QPopularPostPreviewDto(festival.id, festivalTrans.title,
                festivalTrans.addressTag,
                Expressions.constant(Category.FESTIVAL.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(festival, post)
        .innerJoin(festival.festivalTrans, festivalTrans)
        .innerJoin(festival.firstImageFile, imageFile)
        .where(
            festival.id.eq(post.id),
            festival.id.notIn(excludeIds),
            festivalTrans.language.eq(language)
        )
        .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
        .fetchFirst();
  }

  /**
   * 축제 게시물 한국어 주소 조회
   *
   * @param postId 게시물 ID
   * @return 한국어 주소 Optional String 객체
   */
  @Override
  public Optional<String> findKoreanAddress(Long postId) {
    return Optional.ofNullable(
        queryFactory
            .select(festivalTrans.address)
            .from(festival)
            .innerJoin(festival.festivalTrans, festivalTrans)
            .where(festival.id.eq(postId),
                festivalTrans.language.eq(Language.KOREAN))
            .fetchOne()
    );
  }

  @Override
  public PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(festival.id, festivalTrans.title,
                festivalTrans.addressTag,
                Expressions.constant(Category.FESTIVAL.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(festival, post)
        .innerJoin(festival.festivalTrans, festivalTrans)
        .innerJoin(festival.firstImageFile, imageFile)
        .where(
            festival.id.eq(post.id),
            festival.id.eq(postId),
            festivalTrans.language.eq(language)
        )
        .fetchOne();
  }

  private BooleanExpression addressTagCondition(Language language, List<AddressTag> addressTags) {
    if (addressTags == null || addressTags.isEmpty()) {
      return null;
    } else {
      List<String> addressTagFilters = addressTags.stream()
          .map(address -> address.getValueByLocale(language)).toList();
      return festivalTrans.addressTag.in(addressTagFilters);
    }
  }

  /**
   * 공백 제거, 소문자화, '-'와 '_' 제거
   *
   * @param stringExpression 조건절 컬럼
   * @return 정규화된 컬럼
   */
  private StringExpression normalizeStringExpression(StringExpression stringExpression) {
    return Expressions.stringTemplate(
        "replace(replace({0}, '-', ''), '_', '')",
        stringExpression.toLowerCase().trim());
  }

  /**
   * 제목, 주소태그, 내용과 일치하는 키워드 개수 카운팅
   *
   * @param keywords 키워드
   * @return 키워드를 포함하는 조건 개수
   */
  private Expression<Long> countMatchingWithKeyword(List<String> keywords) {
    return Expressions.asNumber(0L)
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(festivalTrans.title),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(festivalTrans.addressTag),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(festivalTrans.content, keywords, 0));
  }

  /**
   * 조건이 키워드를 포함하는지 검사
   *
   * @param condition 테이블 컬럼
   * @param keywords  유저 키워드 리스트
   * @param idx       키워드 인덱스
   * @return
   */
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

  private BooleanExpression periodCondition(LocalDate startDate, LocalDate endDate) {
    // 조건으로 들어오지 않은 경우
    if (startDate == null && endDate == null) {
      return null;
    }
    // 둘 중 하나가 없을 때
    else if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("시작일과 종료일을 모두 입력해야 합니다.");
    }

    return (
        // 축제의 시작일이 필터 시작일 보다 작고 축제의 종료일은 필터의 시작 일보다 클 때
        festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
        // 축제의 기간이 필터 사이에 있을 대
        .or((festival.startDate.goe(startDate)
            .and(festival.endDate.loe(endDate))))
        // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
        .or((festival.startDate.loe(endDate))
            .and(festival.endDate.goe(endDate)));
  }
}
