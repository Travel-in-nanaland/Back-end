package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_CONSENT_NOT_FOUND;

import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentItem;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ConsentUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

  // 이용약관 생성
  public void createMemberConsents(Member member, List<ConsentItem> consentItems) {
    Map<ConsentType, Boolean> consentItemMap = consentItems.stream()
        .collect(Collectors.toMap(
            consentItem -> ConsentType.valueOf(consentItem.getConsentType()),
            ConsentItem::getConsent
        ));

    // 필수 이용약관이 false인 경우
    Boolean termsOfUseConsent = consentItemMap.get(ConsentType.TERMS_OF_USE);
    if (termsOfUseConsent == null || !termsOfUseConsent) {
      throw new BadRequestException(ErrorCode.MEMBER_CONSENT_BAD_REQUEST.getMessage());
    }

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

  // 매일, 동의일로부터 1년 6개월이 지난 경우, 동의 여부를 false로 변환
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void checkTermsValidity() {
    List<MemberConsent> memberConsents = memberRepository.findExpiredMemberConsent();
    if (!memberConsents.isEmpty()) {
      memberConsents.forEach(memberConsent -> memberConsent.updateConsent(false));
    }
  }

  // 이용약관 동의 여부 수정
  @Transactional
  public void updateMemberConsent(MemberInfoDto memberInfoDto, ConsentUpdateDto consentUpdateDto) {
    MemberConsent memberConsent = memberConsentRepository.findByConsentTypeAndMember(
            ConsentType.valueOf(consentUpdateDto.getConsentType()),
            memberInfoDto.getMember())
        .orElseThrow(() -> new NotFoundException(MEMBER_CONSENT_NOT_FOUND.getMessage()));
    memberConsent.updateConsent(consentUpdateDto.getConsent());
  }
}
