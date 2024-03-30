package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberTypeService {

  private final MemberRepository memberRepository;

  @Transactional
  public void updateMemberType(Long memberId, MemberType type) {
    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);

    member.updateMemberType(type);
  }
}
