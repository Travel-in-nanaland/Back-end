package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentItem;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberConsentService {

  private final MemberConsentRepository memberConsentRepository;
  private final MemberRepository memberRepository;

  public void createMemberConsents(Member member, List<ConsentItem> consentItems) {
    List<MemberConsent> memberConsents = consentItems
        .stream().map(consentItem ->
            MemberConsent.builder()
                .member(member)
                .consentType(ConsentType.valueOf(consentItem.getConsentType()))
                .consent(consentItem.getConsent())
                .build())
        .collect(Collectors.toList());
    memberConsentRepository.saveAll(memberConsents);
  }

  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void checkTermsValidity() {
    List<MemberConsent> memberConsents = memberRepository.findExpiredMemberConsent();
    if (!memberConsents.isEmpty()) {
      memberConsents.forEach(memberConsent -> memberConsent.updateConsent(false));
    }
  }

//  @Transactional
//  public void updateConsent(MemberInfoDto memberInfoDto, MemberConsentDTO memberConsentDTO) {
//    for (ConsentItem consentItem : memberConsentDTO.getConsentItems()) {
//      Optional<MemberConsent> memberConsentOptional = memberConsentRepository.findByConsentTypeAndMember(
//          ConsentType.valueOf(consentItem.getConsentType()),
//          memberInfoDto.getMember());
//
//      if (memberConsentOptional.isPresent()) {
//        MemberConsent memberConsent = memberConsentOptional.get();
//        memberConsent.updateConsent(consentItem.getConsent());
//      } else {
//        MemberConsent memberConsent = MemberConsent.builder()
//            .member(memberInfoDto.getMember())
//            .consentType(ConsentType.valueOf(consentItem.getConsentType()))
//            .consent(consentItem.getConsent())
//            .build();
//        memberConsentRepository.save(memberConsent);
//      }
//    }
//  }
}
