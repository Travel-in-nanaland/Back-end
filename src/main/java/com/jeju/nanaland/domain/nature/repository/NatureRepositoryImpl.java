package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPost.post;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse;
import com.jeju.nanaland.domain.nature.dto.NatureSearchDto;
import com.jeju.nanaland.domain.nature.dto.QNatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.QNatureResponse_PreviewDto;
import com.jeju.nanaland.domain.nature.dto.QNatureSearchDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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
public class NatureRepositoryImpl implements NatureRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 7대 자연 정보 조회
   *
   * @param natureId 7대자연 ID
   * @param language 언어
   * @return 7대 자연 정보
   */
  @Override
  public NatureCompositeDto findNatureCompositeDto(Long natureId, Language language) {
    return queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            natureTrans.language,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(natureId)
            .and(natureTrans.language.eq(language))
        )
        .fetchOne();
  }

  @Override
  public NatureCompositeDto findNatureCompositeDtoWithPessimisticLock(Long natureId,
      Language language) {
    return queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            natureTrans.language,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(natureId)
            .and(natureTrans.language.eq(language))
        )
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  /**
   * 게시물의 제목, 주소태그, 키워드, 해시태그 중 하나라도 겹치는 게시물이 있다면 조회 일치한 수, 생성일자 내림차순
   *
   * @param keywords    유저 키워드 리스트
   * @param addressTags 지역필터 리스트
   * @param language    유저 언어
   * @param pageable    페이징
   * @return 검색결과
   */
  @Override
  public Page<NatureSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable) {

    // nature_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(nature.id, nature.id.count())
        .from(nature)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nature.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(nature.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(nature.id),  // key: nature_id
            tuple -> tuple.get(nature.id.count())  // value: 매칭된 키워드 개수
        ));

    List<NatureSearchDto> resultDto = queryFactory
        .select(new QNatureSearchDto(
            nature.id,
            natureTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            nature.createdAt
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.eq(language))
        .where(addressTagCondition(language, addressTags))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (NatureSearchDto natureSearchDto : resultDto) {
      Long id = natureSearchDto.getId();
      natureSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(natureSearchDto -> natureSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<NatureSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(NatureSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(NatureSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<NatureSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  /**
   * 게시물의 제목, 주소태그, 키워드, 해시태그와 모두 겹치는 게시물이 있다면 조회 생성일자 내림차순
   *
   * @param keywords    유저 키워드 리스트
   * @param addressTags 지역필터 리스트
   * @param language    유저 언어
   * @param pageable    페이징
   * @return 검색결과
   */
  @Override
  public Page<NatureSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable) {
    // experience_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(nature.id, nature.id.count())
        .from(nature)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nature.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(nature.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(nature.id),  // key: nature_id
            tuple -> tuple.get(nature.id.count())  // value: 매칭된 키워드 개수
        ));

    List<NatureSearchDto> resultDto = queryFactory
        .select(new QNatureSearchDto(
            nature.id,
            natureTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            nature.createdAt
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.eq(language))
        .where(addressTagCondition(language, addressTags))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (NatureSearchDto natureSearchDto : resultDto) {
      Long id = natureSearchDto.getId();
      natureSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(natureSearchDto -> natureSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<NatureSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(NatureSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<NatureSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  /**
   * 7대 자연 프리뷰 페이징 조회
   *
   * @param language    언어
   * @param addressTags 지역명
   * @param keyword     키워드
   * @param pageable    페이징 정보
   * @return 7대 자연 검색 페이징 정보
   */
  @Override
  public Page<NatureResponse.PreviewDto> findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(
      Language language,
      List<AddressTag> addressTags, String keyword, Pageable pageable) {
    List<NatureResponse.PreviewDto> resultDto = queryFactory
        .select(new QNatureResponse_PreviewDto(
                nature.id,
                natureTrans.title,
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                natureTrans.addressTag
            )
        )
        .from(nature)
        .innerJoin(nature.natureTrans, natureTrans)
        .innerJoin(nature.firstImageFile, imageFile)
        .where(natureTrans.language.eq(language)
            .and(addressTagCondition(language, addressTags))
            .and(natureTrans.title.contains(keyword)))
        .orderBy(nature.priority.asc(), nature.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .innerJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.eq(language)
            .and(addressTagCondition(language, addressTags))
            .and(natureTrans.title.contains(keyword)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  /**
   * 7대 자연 프리뷰 정보 조회
   *
   * @param natureId 7대자연 ID
   * @param language 언어
   * @return 7대 자연 프리뷰 정보
   */
  @Override
  public PostPreviewDto findPostPreviewDto(Long natureId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            nature.id,
            natureTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nature)
        .innerJoin(nature.natureTrans, natureTrans)
        .innerJoin(nature.firstImageFile, imageFile)
        .where(nature.id.eq(natureId),
            natureTrans.language.eq(language))
        .fetchOne();
  }

  @Override
  public List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(nature.id, natureTrans.title,
                natureTrans.addressTag,
                Expressions.constant(Category.NATURE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(nature, post)
        .innerJoin(nature.natureTrans, natureTrans)
        .innerJoin(nature.firstImageFile, imageFile)
        .where(
            nature.id.eq(post.id),
            natureTrans.language.eq(language),
            nature.id.notIn(excludeIds),
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
            new QPopularPostPreviewDto(nature.id, natureTrans.title,
                natureTrans.addressTag,
                Expressions.constant(Category.NATURE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(nature, post)
        .innerJoin(nature.natureTrans, natureTrans)
        .innerJoin(nature.firstImageFile, imageFile)
        .where(
            nature.id.eq(post.id),
            nature.id.notIn(excludeIds),
            natureTrans.language.eq(language)
        )
        .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
        .fetchFirst();
  }

  @Override
  public PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(nature.id, natureTrans.title,
                natureTrans.addressTag,
                Expressions.constant(Category.NATURE.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(nature, post)
        .innerJoin(nature.natureTrans, natureTrans)
        .innerJoin(nature.firstImageFile, imageFile)
        .where(
            nature.id.eq(post.id),
            nature.id.eq(postId),
            natureTrans.language.eq(language)
        )
        .fetchOne();
  }

  /**
   * 자연 게시물 한국어 주소 조회
   *
   * @param postId 게시물 ID
   * @return 한국어 주소 Optional String 객체
   */
  @Override
  public Optional<String> findKoreanAddress(Long postId) {
    return Optional.ofNullable(
        queryFactory
            .select(natureTrans.address)
            .from(nature)
            .innerJoin(nature.natureTrans, natureTrans)
            .where(nature.id.eq(postId),
                natureTrans.language.eq(Language.KOREAN))
            .fetchOne()
    );
  }

  /**
   * 지역명 필터 여부에 따른 조건문 설정
   *
   * @param addressTags 지역명
   * @return BooleanExpression
   */
  private BooleanExpression addressTagCondition(Language language, List<AddressTag> addressTags) {
    if (addressTags == null || addressTags.isEmpty()) {
      return null;
    } else {
      List<String> addressTagFilters = addressTags.stream()
          .map(address -> address.getValueByLocale(language)).toList();
      return natureTrans.addressTag.in(addressTagFilters);
    }
  }

  /**
   * 해시태그가 포함된 7대 자연 ID 리스트 조회
   *
   * @param keyword  키워드
   * @param language 언어
   * @return 7대 자연 ID 리스트
   */
  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(nature.id)
        .from(nature)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nature.id)
            .and(hashtag.category.eq(Category.NATURE))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(nature.id)
        .having(nature.id.count().eq((long) splitKeyword(keyword).size()))
        .fetch();
  }

  /**
   * 키워드 분리
   *
   * @param keyword 키워드
   * @return 분리된 키워드 리스트
   */
  private List<String> splitKeyword(String keyword) {
    String[] tokens = keyword.split("\\s+");
    List<String> tokenList = new ArrayList<>();

    for (String token : tokens) {
      tokenList.add(token.trim());
    }
    return tokenList;
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
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(natureTrans.title),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(natureTrans.addressTag),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(natureTrans.content, keywords, 0));
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
}
