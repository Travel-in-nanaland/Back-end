package com.jeju.nanaland.domain.favorite.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.favorite.dto.QFavoriteResponse_ThumbnailDto is a Querydsl Projection type for ThumbnailDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QFavoriteResponse_ThumbnailDto extends ConstructorExpression<FavoriteResponse.ThumbnailDto> {

    private static final long serialVersionUID = -1740258037L;

    public QFavoriteResponse_ThumbnailDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> thumbnailUrl) {
        super(FavoriteResponse.ThumbnailDto.class, new Class<?>[]{long.class, String.class, String.class}, id, title, thumbnailUrl);
    }

}

