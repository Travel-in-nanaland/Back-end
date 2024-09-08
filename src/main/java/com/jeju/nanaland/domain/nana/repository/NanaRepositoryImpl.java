package com.jeju.nanaland.domain.nana.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaContent.nanaContent;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.dto.QPostCardDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnailPost;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NanaRepositoryImpl implements NanaRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  //최신순으로 4
  @Override
  public List<PreviewDto> findTop4RecentPreviewDtoOrderByCreatedAt(Language language) {
    return queryFactory.select(new QNanaResponse_NanaThumbnail(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading,
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
    List<PreviewDto> resultDto = queryFactory.select(new QNanaResponse_NanaThumbnail(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading,
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
    return queryFactory.select(new QNanaResponse_NanaThumbnail(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading,
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

    List<PreviewDto> resultDto = queryFactory.selectDistinct(new QNanaResponse_NanaThumbnail(
            nana.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading,
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

  @Override
  public PostCardDto findPostCardDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostCardDto(
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
}
