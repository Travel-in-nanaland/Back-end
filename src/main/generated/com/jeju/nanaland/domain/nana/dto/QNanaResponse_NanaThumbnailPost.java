package com.jeju.nanaland.domain.nana.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnailPost is a Querydsl Projection type for NanaThumbnailPost
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QNanaResponse_NanaThumbnailPost extends ConstructorExpression<NanaResponse.NanaThumbnailPost> {

    private static final long serialVersionUID = 634010030L;

    public QNanaResponse_NanaThumbnailPost(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<String> heading) {
        super(NanaResponse.NanaThumbnailPost.class, new Class<?>[]{long.class, String.class, String.class}, id, thumbnailUrl, heading);
    }

}

