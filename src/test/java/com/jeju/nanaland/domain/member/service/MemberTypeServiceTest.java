package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberTypeServiceTest {

  @InjectMocks
  MemberTypeService memberTypeService;

  @Mock
  MemberTravelTypeRepository memberTravelTypeRepository;

  @Mock
  RecommendRepository recommendRepository;

  @DisplayName("타입 수정 성공")
  @Test
  void updateTypeSuccess() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto();

    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(TravelType.GAMGYUL_ICECREAM.name());

    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_ICECREAM)
        .build();

    doReturn(memberTravelType).when(memberTravelTypeRepository)
        .findByTravelType(any(TravelType.class));

    // when
    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);

    // then
    assertThat(memberInfoDto.getMember().getMemberTravelType())
        .isEqualTo(memberTravelType);
  }

  @DisplayName("타입 수정 실패 - 없는 TravelType")
  @Test
  void updateTypeFail() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto();

    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(TravelType.GAMGYUL_ICECREAM.name());

    doReturn(null).when(memberTravelTypeRepository)
        .findByTravelType(any(TravelType.class));

    // when
    RuntimeException runtimeException = assertThrows(RuntimeException.class,
        () -> memberTypeService.updateMemberType(memberInfoDto, updateTypeDto));

    // then
    assertThat(runtimeException.getMessage())
        .isEqualTo(updateTypeDto.getType() + "에 해당하는 타입 정보가 없습니다.");
  }

  private MemberInfoDto createMemberInfoDto() {
    return MemberInfoDto.builder()
        .member(Member.builder().build())
        .language(Language.builder().build())
        .build();
  }
}