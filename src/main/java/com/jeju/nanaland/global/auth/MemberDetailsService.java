package com.jeju.nanaland.global.auth;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

    Member member = memberRepository.findMemberById(Long.parseLong(memberId))
        .orElseThrow(() -> new UsernameNotFoundException(MEMBER_NOT_FOUND.getMessage()));

    List<SimpleGrantedAuthority> authorities = member.getRoleSet().stream()
        .map(role -> new SimpleGrantedAuthority(role.name()))
        .collect(Collectors.toList());

    return new User(String.valueOf(member.getId()), "", authorities);
  }
}
