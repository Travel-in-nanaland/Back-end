package com.jeju.nanaland.domain.restaurant.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPost.post;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurant.restaurant;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantKeyword.restaurantKeyword;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantMenu.restaurantMenu;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantTrans.restaurantTrans;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantSearchDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantSearchDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<RestaurantThumbnail> findRestaurantThumbnails(Language language,
      List<RestaurantTypeKeyword> keywordFilter, List<AddressTag> addressTags, Pageable pageable) {
    List<RestaurantThumbnail> resultDto = queryFactory
        .selectDistinct(new QRestaurantResponse_RestaurantThumbnail(
            restaurant.id,
            restaurantTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            restaurantTrans.addressTag
        ))
        .from(restaurant)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurantKeyword)
        .on(restaurantKeyword.restaurant.eq(restaurant))
        .where(restaurantTrans.language.eq(language)
            .and(keywordCondition(keywordFilter))
            .and(addressTagCondition(language, addressTags)))
        .orderBy(restaurant.priority.desc(),
            restaurant.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .selectDistinct(restaurant.countDistinct())
        .from(restaurant)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurantKeyword)
        .on(restaurantKeyword.restaurant.eq(restaurant))
        .where(restaurantTrans.language.eq(language)
            .and(keywordCondition(keywordFilter))
            .and(addressTagCondition(language, addressTags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public RestaurantCompositeDto findCompositeDtoById(Long postId, Language language) {
    return queryFactory
        .select(new QRestaurantCompositeDto(
            restaurant.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            restaurant.contact,
            restaurantTrans.language,
            restaurantTrans.title,
            restaurantTrans.content,
            restaurantTrans.address,
            restaurantTrans.addressTag,
            restaurantTrans.time,
            restaurant.homepage,
            restaurant.instagram,
            restaurantTrans.service
        ))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .fetchOne();
  }

  @Override
  public RestaurantCompositeDto findCompositeDtoByIdWithPessimisticLock(Long postId,
      Language language) {
    return queryFactory
        .select(new QRestaurantCompositeDto(
            restaurant.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            restaurant.contact,
            restaurantTrans.language,
            restaurantTrans.title,
            restaurantTrans.content,
            restaurantTrans.address,
            restaurantTrans.addressTag,
            restaurantTrans.time,
            restaurant.homepage,
            restaurant.instagram,
            restaurantTrans.service
        ))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  @Override
  public Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSet(Long postId) {
    Map<Long, Set<RestaurantTypeKeyword>> map = queryFactory
        .selectFrom(restaurantKeyword)
        .where(restaurantKeyword.restaurant.id.eq(postId))
        .transform(GroupBy.groupBy(restaurantKeyword.restaurant.id)
            .as(GroupBy.set(restaurantKeyword.restaurantTypeKeyword)));

    return map.getOrDefault(postId, Collections.emptySet());
  }

  @Override
  public Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSetWithPessimisticLock(Long postId) {
    Map<Long, Set<RestaurantTypeKeyword>> map = queryFactory
        .selectFrom(restaurantKeyword)
        .where(restaurantKeyword.restaurant.id.eq(postId))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .transform(GroupBy.groupBy(restaurantKeyword.restaurant.id)
            .as(GroupBy.set(restaurantKeyword.restaurantTypeKeyword)));

    return map.getOrDefault(postId, Collections.emptySet());
  }

  @Override
  public List<RestaurantMenuDto> getRestaurantMenuList(Long postId, Language language) {
    return queryFactory
        .select(new QRestaurantResponse_RestaurantMenuDto(
            restaurantMenu.menuName,
            restaurantMenu.price,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(restaurantMenu)
        .leftJoin(restaurantMenu.firstImageFile, imageFile)
        .where(restaurantMenu.restaurantTrans.restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .fetch();
  }

  @Override
  public List<RestaurantMenuDto> getRestaurantMenuListWithPessimisticLock(Long postId,
      Language language) {
    return queryFactory
        .select(new QRestaurantResponse_RestaurantMenuDto(
            restaurantMenu.menuName,
            restaurantMenu.price,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(restaurantMenu)
        .leftJoin(restaurantMenu.firstImageFile, imageFile)
        .where(restaurantMenu.restaurantTrans.restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetch();
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
  public Page<RestaurantSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      Language language, Pageable pageable) {
    // restaurant_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(restaurant.id, restaurant.id.count())
        .from(restaurant)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(restaurant.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(restaurant.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(restaurant.id),  // key: restaurant_id
            tuple -> tuple.get(restaurant.id.count())  // value: 매칭된 키워드 개수
        ));

    List<RestaurantSearchDto> resultDto = queryFactory
        .select(new QRestaurantSearchDto(
            restaurant.id,
            restaurantTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            restaurant.createdAt
        ))
        .from(restaurant)
        .leftJoin(restaurant.firstImageFile, imageFile)
        .leftJoin(restaurant.restaurantTrans, restaurantTrans)
        .on(restaurantTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (RestaurantSearchDto restaurantSearchDto : resultDto) {
      Long id = restaurantSearchDto.getId();
      restaurantSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(restaurantSearchDto -> restaurantSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<RestaurantSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(RestaurantSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(RestaurantSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<RestaurantSearchDto> finalList = resultList.subList(startIdx, endIdx);
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
  public Page<RestaurantSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language, Pageable pageable) {
    // restaurant_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(restaurant.id, restaurant.id.count())
        .from(restaurant)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(restaurant.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(restaurant.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(restaurant.id),  // key: restaurant_id
            tuple -> tuple.get(restaurant.id.count())  // value: 매칭된 키워드 개수
        ));

    List<RestaurantSearchDto> resultDto = queryFactory
        .select(new QRestaurantSearchDto(
            restaurant.id,
            restaurantTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            restaurant.createdAt
        ))
        .from(restaurant)
        .leftJoin(restaurant.firstImageFile, imageFile)
        .leftJoin(restaurant.restaurantTrans, restaurantTrans)
        .on(restaurantTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (RestaurantSearchDto restaurantSearchDto : resultDto) {
      Long id = restaurantSearchDto.getId();
      restaurantSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(restaurantSearchDto -> restaurantSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<RestaurantSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(RestaurantSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<RestaurantSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            restaurant.id,
            restaurantTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(
            restaurant.id.eq(postId),
            restaurantTrans.language.eq(language))
        .fetchOne();
  }

  @Override
  public List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(restaurant.id, restaurantTrans.title,
                restaurantTrans.addressTag,
                Expressions.constant(Category.RESTAURANT.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(restaurant, post)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(
            restaurant.id.eq(post.id),
            restaurantTrans.language.eq(language),
            restaurant.id.notIn(excludeIds),
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
            new QPopularPostPreviewDto(restaurant.id, restaurantTrans.title,
                restaurantTrans.addressTag,
                Expressions.constant(Category.RESTAURANT.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(restaurant, post)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(
            restaurant.id.eq(post.id),
            restaurant.id.notIn(excludeIds),
            restaurantTrans.language.eq(language)
        )
        .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
        .fetchFirst();
  }

  @Override
  public PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(restaurant.id, restaurantTrans.title,
                restaurantTrans.addressTag,
                Expressions.constant(Category.RESTAURANT.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(restaurant, post)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(
            restaurant.id.eq(post.id),
            restaurant.id.eq(postId),
            restaurantTrans.language.eq(language)
        )
        .fetchOne();
  }

  /**
   * 맛집 게시물 한국어 주소 조회
   *
   * @param postId 게시물 ID
   * @return 한국어 주소 Optional String 객체
   */
  @Override
  public Optional<String> findKoreanAddress(Long postId) {
    return Optional.ofNullable(
        queryFactory
            .select(restaurantTrans.address)
            .from(restaurant)
            .innerJoin(restaurant.restaurantTrans, restaurantTrans)
            .where(restaurant.id.eq(postId),
                restaurantTrans.language.eq(Language.KOREAN))
            .fetchOne()
    );
  }

  private BooleanExpression addressTagCondition(Language language, List<AddressTag> addressTags) {
    if (addressTags.isEmpty()) {
      return null;
    } else {
      List<String> addressTagFilters = addressTags.stream()
          .map(address -> address.getValueByLocale(language)).toList();
      return restaurantTrans.addressTag.in(addressTagFilters);
    }
  }

  private BooleanExpression keywordCondition(List<RestaurantTypeKeyword> keywordFilterList) {
    if (keywordFilterList.isEmpty()) {
      return null;
    } else {
      return restaurantKeyword.restaurantTypeKeyword.in(keywordFilterList);
    }
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(restaurant.id)
        .from(restaurant)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(restaurant.id)
            .and(hashtag.category.eq(Category.RESTAURANT))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(restaurant.id)
        .having(restaurant.id.count().eq(splitKeyword(keyword).stream().count()))
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
  public List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language) {
    return queryFactory
        .select(
            new QReviewResponse_SearchPostForReviewDto(restaurant.id,
                Expressions.constant(Category.RESTAURANT.name()),
                restaurantTrans.title, restaurant.firstImageFile, restaurantTrans.address))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .where(restaurantTrans.language.eq(language))
        .fetch();
  }

  @Override
  public List<Long> findAllIds() {
    return queryFactory
        .select(restaurant.id)
        .from(restaurant)
        .fetch();
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
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(restaurantTrans.title),
            keywords, 0))
        .add(
            countMatchingConditionWithKeyword(normalizeStringExpression(restaurantTrans.addressTag),
                keywords, 0))
        .add(countMatchingConditionWithKeyword(restaurantTrans.content, keywords, 0));
  }

  /**
   * 조건이 키워드를 포함하는지 검사
   *
   * @param condition 테이블 컬럼
   * @param keywords  유저 키워드 리스트
   * @param idx       키워드 인덱스
   * @return 매칭된 수
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
