package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.entity.ConsentType;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberConsentService {

  private final MemberConsentRepository memberConsentRepository;

  public void createMemberConsents(Member member) {
    List<MemberConsent> memberConsents = Arrays.stream(ConsentType.values())
        .map(type -> MemberConsent.builder()
            .member(member)
            .consentType(type)
            .consent(false)
            .build())
        .collect(Collectors.toList());
    memberConsentRepository.saveAll(memberConsents);
  }
}
