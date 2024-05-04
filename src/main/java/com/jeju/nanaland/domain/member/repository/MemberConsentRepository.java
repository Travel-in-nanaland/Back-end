package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.ConsentType;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {

  Optional<MemberConsent> findByConsentTypeAndMember(ConsentType consentType, Member member);
}
