package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByProviderAndProviderId(Provider provider, Long providerId);

  @Query("select m from Member m join fetch m.roleSet where m.id = :memberId")
  Optional<Member> findMemberById(@Param("memberId") Long memberId);
}

