package com.jeju.nanaland.domain.nana.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnail is a Querydsl Projection type for NanaThumbnail
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QNanaResponse_NanaThumbnail extends ConstructorExpression<NanaResponse.NanaThumbnail> {

    private static final long serialVersionUID = -928263186L;

    public QNanaResponse_NanaThumbnail(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<String> version, com.querydsl.core.types.Expression<String> subHeading, com.querydsl.core.types.Expression<String> heading) {
        super(NanaResponse.NanaThumbnail.class, new Class<?>[]{long.class, String.class, String.class, String.class, String.class}, id, thumbnailUrl, version, subHeading, heading);
    }

}

