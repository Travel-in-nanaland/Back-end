package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentItem;
import com.jeju.nanaland.domain.member.dto.MemberRequest.MemberConsentDTO;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.ConsentType;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void updateConsent(MemberInfoDto memberInfoDto, MemberConsentDTO memberConsentDTO) {
    for (ConsentItem consentItem : memberConsentDTO.getConsentItems()) {
      Optional<MemberConsent> memberConsentOptional = memberConsentRepository.findByConsentTypeAndMember(
          ConsentType.valueOf(consentItem.getConsentType()),
          memberInfoDto.getMember());

      if (memberConsentOptional.isPresent()) {
        MemberConsent memberConsent = memberConsentOptional.get();
        memberConsent.updateConsent(consentItem.getConsent());
      } else {
        MemberConsent memberConsent = MemberConsent.builder()
            .member(memberInfoDto.getMember())
            .consentType(ConsentType.valueOf(consentItem.getConsentType()))
            .consent(consentItem.getConsent())
            .build();
        memberConsentRepository.save(memberConsent);
      }
    }
  }
}
