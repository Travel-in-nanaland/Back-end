package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class RecommendRepositoryTest {

  @Autowired
  RecommendRepository recommendRepository;

  @DisplayName("타입을 통해 추천 게시물 리스트 조회")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void findAllByMemberTravelType(TravelType travelType) {
    // given
    MemberTravelType memberTravelType = new MemberTravelType(travelType);

    // when
    recommendRepository.findAllByMemberTravelType(memberTravelType);

    // then
  }
}
