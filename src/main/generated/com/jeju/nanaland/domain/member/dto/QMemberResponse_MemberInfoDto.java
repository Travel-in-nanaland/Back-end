package com.jeju.nanaland.domain.member.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.jeju.nanaland.domain.member.dto.QMemberResponse_MemberInfoDto is a Querydsl Projection type for MemberInfoDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberResponse_MemberInfoDto extends ConstructorExpression<MemberResponse.MemberInfoDto> {

    private static final long serialVersionUID = -715842785L;

    public QMemberResponse_MemberInfoDto(com.querydsl.core.types.Expression<? extends com.jeju.nanaland.domain.member.entity.Member> member, com.querydsl.core.types.Expression<? extends com.jeju.nanaland.domain.common.entity.Language> language) {
        super(MemberResponse.MemberInfoDto.class, new Class<?>[]{com.jeju.nanaland.domain.member.entity.Member.class, com.jeju.nanaland.domain.common.entity.Language.class}, member, language);
    }

}

