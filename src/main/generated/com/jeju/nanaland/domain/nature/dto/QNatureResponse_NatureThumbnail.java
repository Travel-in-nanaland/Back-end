package com.jeju.nanaland.domain.nature.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.nature.dto.QNatureResponse_NatureThumbnail is a Querydsl Projection type for NatureThumbnail
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QNatureResponse_NatureThumbnail extends ConstructorExpression<NatureResponse.NatureThumbnail> {

    private static final long serialVersionUID = -2109357907L;

    public QNatureResponse_NatureThumbnail(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<String> addressTag, com.querydsl.core.types.Expression<Boolean> isFavorite) {
        super(NatureResponse.NatureThumbnail.class, new Class<?>[]{long.class, String.class, String.class, String.class, boolean.class}, id, title, thumbnailUrl, addressTag, isFavorite);
    }

}

