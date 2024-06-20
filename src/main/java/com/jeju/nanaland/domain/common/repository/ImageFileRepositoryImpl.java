package com.jeju.nanaland.domain.common.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QPostImageFile.postImageFile;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImageFileRepositoryImpl implements ImageFileRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<ImageFileDto> findPostImageFiles(Long postId) {
    return queryFactory
        .select(Projections.constructor(ImageFileDto.class,
            imageFile.originUrl,
            imageFile.thumbnailUrl))
        .from(imageFile)
        .innerJoin(postImageFile).on(postImageFile.imageFile.eq(imageFile))
        .where(postImageFile.post.id.eq(postId))
        .orderBy(imageFile.id.asc())
        .fetch();
  }
}
