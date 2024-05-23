package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentItem;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    Map<ConsentType, Boolean> consentItemMap = consentItems.stream()
        .collect(Collectors.toMap(
            consentItem -> ConsentType.valueOf(consentItem.getConsentType()),
            ConsentItem::getConsent
        ));

    List<MemberConsent> memberConsents = Arrays.stream(ConsentType.values())
        .map(consentType -> {
          Boolean consent = consentItemMap.get(consentType);
          return MemberConsent.builder()
              .member(member)
              .consentType(consentType)
              .consent(consent != null && consent)
              .build();
        }).collect(Collectors.toList());

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

  @Transactional
  public void updateMemberConsent(MemberInfoDto memberInfoDto, ConsentUpdateDto consentUpdateDto) {
    Optional<MemberConsent> memberConsentOptional = memberConsentRepository.findByConsentTypeAndMember(
        ConsentType.valueOf(consentUpdateDto.getConsentType()),
        memberInfoDto.getMember());
    if (memberConsentOptional.isPresent()) {
      MemberConsent memberConsent = memberConsentOptional.get();
      memberConsent.updateConsent(consentUpdateDto.getConsent());
    } else {
      MemberConsent memberConsent = MemberConsent.builder()
          .member(memberInfoDto.getMember())
          .consentType(ConsentType.valueOf(consentUpdateDto.getConsentType()))
          .consent(consentUpdateDto.getConsent())
          .build();
      memberConsentRepository.save(memberConsent);
    }
  }
}
