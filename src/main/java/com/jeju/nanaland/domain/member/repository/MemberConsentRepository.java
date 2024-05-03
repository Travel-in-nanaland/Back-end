package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.MemberConsent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {

}
