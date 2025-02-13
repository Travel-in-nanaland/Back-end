package com.jeju.nanaland.domain.nana.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaContent.nanaContent;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.hashtag.entity.QKeyword;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.dto.NanaSearchDto;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_PreviewDto;
import com.jeju.nanaland.domain.nana.dto.QNanaSearchDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NanaRepositoryImpl implements NanaRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  //최신순으로 4
  @Override
  public List<PreviewDto> findTop4PreviewDtoOrderByCreatedAt(Language language) {
    return queryFactory.select(new QNanaResponse_PreviewDto(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.subHeading,
            nanaTitle.heading,
            nana.createdAt
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .where((nanaTitle.language.eq(language)))
        .orderBy(nanaTitle.createdAt.desc())
        .limit(4L)
        .fetch();
  }

  // 모든 Nana 썸네일 가져오기
  @Override
  public Page<PreviewDto> findAllPreviewDtoOrderByCreatedAt(Language language,
      Pageable pageable) {
    List<PreviewDto> resultDto = queryFactory.select(new QNanaResponse_PreviewDto(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.subHeading,
            nanaTitle.heading,
            nana.createdAt
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .where((nanaTitle.language.eq(language)))
        .orderBy(nanaTitle.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nanaTitle.count())
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .where((nanaTitle.language.eq(language)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public List<PreviewDto> findRecommendPreviewDto(Language language) {
    return queryFactory.select(new QNanaResponse_PreviewDto(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.subHeading,
            nanaTitle.heading,
            nana.createdAt
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .where((nanaTitle.language.eq(language)))
        .orderBy(nanaTitle.modifiedAt.desc())
        .limit(4L)
        .fetch();
  }

  @Override
  public Page<PreviewDto> searchNanaThumbnailDtoByKeyword(String keyword, Language language,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, language);

    List<PreviewDto> resultDto = queryFactory.selectDistinct(new QNanaResponse_PreviewDto(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.subHeading,
            nanaTitle.heading,
            nana.createdAt
        ))
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(nana.firstImageFile, imageFile)
        .where(nanaTitle.heading.contains(keyword)
            .or(nanaContent.title.contains(keyword))
            .or(nanaContent.content.contains(keyword))
            .or(nanaContent.id.in(idListContainAllHashtags)))
        .orderBy(nanaTitle.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nana.id.countDistinct())
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(nana.firstImageFile, imageFile)
        .where(nanaTitle.heading.contains(keyword)
            .or(nanaContent.title.contains(keyword))
            .or(nanaContent.content.contains(keyword))
            .or(nanaContent.id.in(idListContainAllHashtags)));

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
  public Page<NanaSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords, Language language,
      Pageable pageable) {
    // nana_id 별로 nana_content_id가 가진 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(nana.id, nana.id.count())
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nanaContent.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(nana.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(nana.id),  // key: nana_id
            tuple -> tuple.get(nana.id.count())  // value: 매칭된 키워드 개수
        ));

    List<NanaSearchDto> resultDto = queryFactory
        .select(new QNanaSearchDto(
            nana.id,
            nanaTitle.heading,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            getMaxMatchingCountWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            nana.createdAt
        ))
        .from(nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .on(nanaTitle.language.eq(language))
        .groupBy(nana.id, nanaTitle.heading, imageFile.originUrl, imageFile.thumbnailUrl,
            nana.createdAt)
        .fetch();

    // key: nana_id, value: nanaContent 중 키워드와 가장 많이 일치한 수
    Map<Long, Long> nanaMap = new HashMap<>();
    for (NanaSearchDto nanaSearchDto : resultDto) {
      Long nanaId = nanaSearchDto.getId();
      Long matchedCount = nanaSearchDto.getMatchedCount();
      nanaMap.put(nanaId, Math.max(nanaMap.getOrDefault(nanaId, 0L), matchedCount));
    }

    // 해시태그 값을 matchedCount에 더해줌
    for (NanaSearchDto nanaSearchDto : resultDto) {
      Long id = nanaSearchDto.getId();
      nanaSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 0이라면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(nanaSearchDto -> nanaSearchDto.getMatchedCount() > 0)
        .toList();

    // 매칭된 키워드 수 내림차순, 생성날짜 내림차순 정렬
    List<NanaSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(NanaSearchDto::getMatchedCount,
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(NanaSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<NanaSearchDto> finalList = resultList.subList(startIdx, endIdx);
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
  public Page<NanaSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language,
      Pageable pageable) {
    // nana_id 별로 nana_content_id가 가진 해시태그가 검색어 키워드 중 몇개를 포함하는지 계산
    List<Tuple> keywordMatchQuery = queryFactory
        .select(nana.id, nana.id.count())
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nanaContent.id)
            .and(hashtag.language.eq(language)))
        .innerJoin(hashtag.keyword, QKeyword.keyword)
        .where(QKeyword.keyword.content.toLowerCase().trim().in(keywords))
        .groupBy(nana.id)
        .fetch();

    Map<Long, Long> keywordMatchMap = keywordMatchQuery.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(nana.id),  // key: nana_id
            tuple -> tuple.get(nana.id.count())  // value: 매칭된 키워드 개수
        ));

    List<NanaSearchDto> resultDto = queryFactory
        .select(new QNanaSearchDto(
            nana.id,
            nanaTitle.heading,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            getMaxMatchingCountWithKeyword(keywords),  // 제목, 내용, 지역정보와 매칭되는 키워드 개수
            nana.createdAt
        ))
        .from(nana)
        .leftJoin(nana.firstImageFile, imageFile)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .on(nanaTitle.language.eq(language))
        .groupBy(nana.id, nanaTitle.heading, imageFile.originUrl, imageFile.thumbnailUrl,
            nana.createdAt)
        .fetch();

    // key: nana_id, value: nanaContent 중 키워드와 가장 많이 일치한 수
    Map<Long, Long> nanaMap = new HashMap<>();
    for (NanaSearchDto nanaSearchDto : resultDto) {
      Long nanaId = nanaSearchDto.getId();
      Long matchedCount = nanaSearchDto.getMatchedCount();
      nanaMap.put(nanaId, Math.max(nanaMap.getOrDefault(nanaId, 0L), matchedCount));
    }

    // 해시태그 값을 matchedCount에 더해줌
    for (NanaSearchDto nanaSearchDto : resultDto) {
      Long id = nanaSearchDto.getId();
      nanaSearchDto.addMatchedCount(keywordMatchMap.getOrDefault(id, 0L));
    }
    // matchedCount가 키워드 개수와 다르다면 검색결과에서 제거
    resultDto = resultDto.stream()
        .filter(nanaSearchDto -> nanaSearchDto.getMatchedCount() >= keywords.size())
        .toList();

    // 생성날짜 내림차순 정렬
    List<NanaSearchDto> resultList = new ArrayList<>(resultDto);
    resultList.sort(Comparator
        .comparing(NanaSearchDto::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

    // 페이징 처리
    int startIdx = pageable.getPageSize() * pageable.getPageNumber();
    int endIdx = Math.min(startIdx + pageable.getPageSize(), resultList.size());
    List<NanaSearchDto> finalList = resultList.subList(startIdx, endIdx);
    final Long total = Long.valueOf(resultDto.size());

    return PageableExecutionUtils.getPage(finalList, pageable, () -> total);

  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            nana.id,
            nanaTitle.heading,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(nana)
        .innerJoin(nanaTitle).on(nana.eq(nanaTitle.nana))
        .innerJoin(nana.firstImageFile, imageFile)
        .where(
            nana.id.eq(postId),
            nanaTitle.language.eq(language))
        .fetchOne();
  }

  public NanaThumbnailPost findNanaThumbnailPostDto(Long id, Language language) {
    return queryFactory
        .select(new QNanaResponse_NanaThumbnailPost(
            nanaTitle.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nanaTitle.heading
        ))
        .from(nanaTitle)
        .leftJoin(nana.firstImageFile, imageFile)
        .where(nanaTitle.nana.id.eq(id)
            .and(nanaTitle.language.eq(language)))
        .fetchOne();
  }

  /**
   * 나나스픽 게시물 한국어 주소 조회
   *
   * @param postId 게시물 ID
   * @return 한국어 주소 Optional String 객체
   * @throws ServerErrorException NANA_CONTENT의 우선순위가 중복됨
   */
  @Override
  public Optional<String> findKoreanAddress(Long postId, Long number) {
    List<NanaContent> nanaContents = queryFactory
        .select(nanaContent)
        .from(nana)
        .innerJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana)
            .and(nanaTitle.language.eq(Language.KOREAN)))
        .innerJoin(nanaContent)
        .on(nanaContent.nanaTitle.eq(nanaTitle)
            .and(nanaContent.priority.eq(number)))
        .where(nana.id.eq(postId))
        .fetch();

    // NANA_CONTENT의 priority 값의 중복이 발생
    if (nanaContents.size() > 1) {
      throw new ServerErrorException("나나스픽 구성 내용 우선순위 간의 중복이 발생했습니다.");
    }

    if (!nanaContents.isEmpty()) {
      NanaContent nanaContent = nanaContents.get(0);
      if (nanaContent.getInfoList() != null) {
        return nanaContent.getInfoList().stream()
            .filter(info -> info.getInfoType().equals(InfoType.ADDRESS))
            .map(NanaAdditionalInfo::getDescription)
            .findFirst();
      }
    }
    return Optional.empty();
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(nana.id)
        .from(nana)
        .leftJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
        .leftJoin(nanaContent)
        .on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nanaContent.id)
            .and(hashtag.category.eq(Category.NANA_CONTENT))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(nana.id)
        .having(nana.id.count().eq(splitKeyword(keyword).stream().count()))
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
  private Expression<Long> getMaxMatchingCountWithKeyword(List<String> keywords) {
    return Expressions.asNumber(0L)
        .add(countMatchingConditionWithKeyword(normalizeStringExpression(nanaTitle.heading),
            keywords, 0))
        .add(countMatchingConditionWithKeyword(nanaContent.content, keywords, 0))
        .max();
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
