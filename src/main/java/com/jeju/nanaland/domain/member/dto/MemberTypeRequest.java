package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.member.entity.MemberType;
import lombok.Data;

@Data
public class MemberTypeRequest {

  private MemberType type;
}
