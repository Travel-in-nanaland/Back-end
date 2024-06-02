package com.jeju.nanaland.domain.member.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.member.dto.QMemberResponse_RecommendPostDto is a Querydsl Projection type for RecommendPostDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberResponse_RecommendPostDto extends ConstructorExpression<MemberResponse.RecommendPostDto> {

    private static final long serialVersionUID = -1954149637L;

    public QMemberResponse_RecommendPostDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<com.jeju.nanaland.domain.common.data.CategoryContent> categoryContent, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> introduction) {
        super(MemberResponse.RecommendPostDto.class, new Class<?>[]{long.class, com.jeju.nanaland.domain.common.data.CategoryContent.class, String.class, String.class, String.class}, id, categoryContent, thumbnailUrl, title, introduction);
    }

}

