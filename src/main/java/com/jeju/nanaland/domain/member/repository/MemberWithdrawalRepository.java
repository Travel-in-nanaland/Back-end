package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.MemberWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {

}
