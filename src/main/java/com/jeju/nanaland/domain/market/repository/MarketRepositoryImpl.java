package com.jeju.nanaland.domain.market.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPost.post;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketSearchDto;
import com.jeju.nanaland.domain.market.dto.QMarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.QMarketResponse_MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.QMarketSearchDto;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class MarketRepositoryImpl implements MarketRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public MarketCompositeDto findCompositeDtoById(Long id, Language language) {
    return queryFactory
        .select(new QMarketCompositeDto(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            market.contact,
            market.homepage,
            marketTrans.language,
            marketTrans.title,
            marketTrans.content,
            marketTrans.address,
            marketTrans.addressTag,
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(market.id.eq(id).and(marketTrans.language.eq(language)))
        .fetchOne();
  }

  @Override
  public MarketCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language language) {
    return queryFactory
        .select(new QMarketCompositeDto(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            market.contact,
            market.homepage,
            marketTrans.language,
            marketTrans.title,
            marketTrans.content,
            marketTrans.address,
            marketTrans.addressTag,
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(market.id.eq(id).and(marketTrans.language.eq(language)))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  @Override
  public Page<MarketThumbnail> findMarketThumbnails(Language language,
      List<AddressTag> addressTags, Pageable pageable) {
    List<MarketThumbnail> resultDto = queryFactory
        .select(new QMarketResponse_MarketThumbnail(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            marketTrans.title,
            marketTrans.addressTag
        ))
        .from(market)
        .innerJoin(market.firstImageFile, imageFile)
        .innerJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.eq(language)
            .and(addressTagCondition(language, addressTags)))
        .orderBy(market.priority.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .innerJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.eq(language)
            .and(addressTagCondition(language, addressTags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
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
  public Page<MarketSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      Language language, Pageable pageable) {
    // market_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(market.id, market.id.count())
        .from(market)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(market.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(market.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(market.id),  // key: market_id
            tuple -> tuple.get(market.id.count())  // value: 매칭된 키워드 개수
        ));

    List<MarketSearchDto> resultDto = queryFactory
        .select(new QMarketSearchDto(
            market.id,
            marketTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            market.createdAt
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (MarketSearchDto marketSearchDto : resultDto) {
      Long id = marketSearchDto.getId();
      marketSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(marketSearchDto -> marketSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<MarketSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(MarketSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(MarketSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<MarketSearchDto> finalList = resultList.subList(startIdx, endIdx);
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
  public Page<MarketSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language, Pageable pageable) {
    // market_id를 가진 게시물의 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(market.id, market.id.count())
        .from(market)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(market.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(market.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(market.id),  // key: market_id
            tuple -> tuple.get(market.id.count())  // value: 매칭된 키워드 개수
        ));

    List<MarketSearchDto> resultDto = queryFactory
        .select(new QMarketSearchDto(
            market.id,
            marketTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            countMatchingWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            market.createdAt
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.eq(language))
        .fetch();

    // 해시태그 값을 matchedCount에 더해줌
    for (MarketSearchDto marketSearchDto : resultDto) {
      Long id = marketSearchDto.getId();
      marketSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(marketSearchDto -> marketSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<MarketSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(MarketSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<MarketSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);
  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            market.id,
            marketTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(market)
        .innerJoin(market.marketTrans, marketTrans)
        .innerJoin(market.firstImageFile, imageFile)
        .where(
            market.id.eq(postId),
            marketTrans.language.eq(language))
        .fetchOne();
  }

  @Override
  public List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(market.id, marketTrans.title,
                marketTrans.addressTag,
                Expressions.constant(Category.MARKET.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(market, post)
        .innerJoin(market.marketTrans, marketTrans)
        .innerJoin(market.firstImageFile, imageFile)
        .where(
            market.id.eq(post.id),
            marketTrans.language.eq(language),
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
            new QPopularPostPreviewDto(market.id, marketTrans.title,
                marketTrans.addressTag,
                Expressions.constant(Category.MARKET.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(market, post)
        .innerJoin(market.marketTrans, marketTrans)
        .innerJoin(market.firstImageFile, imageFile)
        .where(
            market.id.eq(post.id),
            market.id.notIn(excludeIds),
            marketTrans.language.eq(language)
        )
        .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
        .fetchFirst();
  }

  @Override
  public PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId) {
    return queryFactory
        .select(
            new QPopularPostPreviewDto(market.id, marketTrans.title,
                marketTrans.addressTag,
                Expressions.constant(Category.MARKET.name()),
                imageFile.originUrl,
                imageFile.thumbnailUrl,
                post.viewCount
            ))
        .from(market, post)
        .innerJoin(market.marketTrans, marketTrans)
        .innerJoin(market.firstImageFile, imageFile)
        .where(
            market.id.eq(post.id),
            market.id.eq(postId),
            marketTrans.language.eq(language)
        )
        .fetchOne();
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(market.id)
        .from(market)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(market.id)
            .and(hashtag.category.eq(Category.MARKET))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(market.id)
        .having(market.id.count().eq(splitKeyword(keyword).stream().count()))
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
      return marketTrans.addressTag.in(addressTagFilters);
    }
  }

  /**
   * 제목, 주소태그, 내용과 일치하는 키워드 개수 카운팅
   *
   * @param keywords 키워드
   * @return 키워드를 포함하는 조건 개수
   */
  private Expression<Long> countMatchingWithKeyword(List<String> keywords) {
    return Expressions.asNumber(0L)
        .add(countMatchingConditionWithKeyword(marketTrans.title.toLowerCase().trim(), keywords,
            0))
        .add(countMatchingConditionWithKeyword(marketTrans.addressTag.toLowerCase().trim(),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(marketTrans.content, keywords, 0));
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
