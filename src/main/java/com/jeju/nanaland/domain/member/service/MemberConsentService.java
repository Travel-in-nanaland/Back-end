package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDateTime;
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

  /**
   * 이용약관 생성
   *
   * @param member       회원 객체
   * @param consentItems 이용약관 동의 여부
   * @throws BadRequestException 필수 이용약관 동의가 제공되지 않았거나 동의하지 않은 경우
   */
  @Transactional
  public void createMemberConsents(Member member, List<MemberRequest.ConsentItemDto> consentItems) {
    Map<ConsentType, Boolean> consentStates = consentItems.stream()
        .collect(Collectors.toMap(
            consentItem -> ConsentType.valueOf(consentItem.getConsentType()),
            MemberRequest.ConsentItemDto::getConsent
        ));

    // 필수 이용약관이 false인 경우
    Boolean termsOfUseConsent = consentStates.get(ConsentType.TERMS_OF_USE);
    if (termsOfUseConsent == null || !termsOfUseConsent) {
      throw new BadRequestException(ErrorCode.MEMBER_CONSENT_BAD_REQUEST.getMessage());
    }

    List<MemberConsent> memberConsents = Arrays.stream(ConsentType.values())
        .map(consentType -> {
          Boolean consent = consentStates.get(consentType);
          return MemberConsent.builder()
              .member(member)
              .consentType(consentType)
              .consent(consent != null && consent)
              .build();
        }).collect(Collectors.toList());

    memberConsentRepository.saveAll(memberConsents);
  }

  /**
   * 매일 0시 0분 0초에 실행되는 동의 여부 관리 스케줄러. 회원이 동의한 이용약관에 대해, 동의일자가 1년 6개월이 지난 경우, false로 변환.
   */
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void checkTermsValidity() {
    List<MemberConsent> memberConsents = memberRepository.findAllExpiredMemberConsent();
    if (!memberConsents.isEmpty()) {
      memberConsents.forEach(memberConsent -> memberConsent.updateConsent(false));
    }
  }

  /**
   * 이용약관 동의 여부 수정
   *
   * @param memberInfoDto    회원 정보
   * @param consentUpdateDto 이용약관 수정 정보
   * @throws NotFoundException 존재하는 이용약관이 없는 경우
   */
  @Transactional
  public boolean updateMemberConsent(MemberInfoDto memberInfoDto,
      MemberRequest.ConsentUpdateDto consentUpdateDto) {

    ConsentType requestedConsentType = ConsentType.valueOf(consentUpdateDto.getConsentType());
    boolean requestedConsentStatus = consentUpdateDto.getConsent();

    MemberConsent memberConsent = memberConsentRepository.findByConsentTypeAndMember(
            requestedConsentType,
            memberInfoDto.getMember())
        .orElse(null);

    // 요청된 동의 내역이 없다면 생성
    if (memberConsent == null) {
      MemberConsent newMemberConsent = MemberConsent.builder()
          .member(memberInfoDto.getMember())
          .consentType(requestedConsentType)
          .consentDate(LocalDateTime.now())
          .consent(requestedConsentStatus)
          .build();

      memberConsentRepository.save(newMemberConsent);
      // 처리 후 동의 상태 반환
      return newMemberConsent.getConsent();
    }

    // 요청한 동의 상태로 변경
    memberConsent.updateConsent(requestedConsentStatus);
    // 처리 후 동의 상태 반환
    return memberConsent.getConsent();
  }
}
