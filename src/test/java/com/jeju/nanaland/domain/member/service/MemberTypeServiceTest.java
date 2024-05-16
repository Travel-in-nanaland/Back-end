package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentMatcher;
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
  @ParameterizedTest
  @EnumSource(value = TravelType.class)
  void updateTypeSuccess(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, TravelType.NONE);

    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(travelType.name());

    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(travelType)
        .build();

    doReturn(memberTravelType).when(memberTravelTypeRepository)
        .findByTravelType(any(TravelType.class));

    // when
    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);

    // then
    assertThat(memberInfoDto.getMember().getMemberTravelType())
        .isEqualTo(memberTravelType);
    verify(memberTravelTypeRepository, times(1))
        .findByTravelType(any(TravelType.class));
  }

  @DisplayName("타입 수정 실패 - 없는 TravelType")
  @Test
  void updateTypeFail() {
    /**
     * given
     *
     * TravelType에 GAMGYUL_ICECREAM이 존재하지만 MemberTravelType 테이블에는 없는 경우
     */
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, TravelType.NONE);

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

  @DisplayName("추천 게시물 반환 성공 - NONE 타입이 아닐 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void getRecommendPostsSuccess(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);
    Locale locale = memberInfoDto.getLanguage().getLocale();

    doReturn(recommendList(travelType)).when(recommendRepository)
        .findAllByMemberTravelType(any(MemberTravelType.class));
    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
        .findNatureRecommendPostDto(1L, locale, travelType);
    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
        .findExperienceRecommendPostDto(2L, locale, travelType);

    // when
    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);

    // then
    assertThat(resultDto.size()).isEqualTo(2);

    // verify
    verify(recommendRepository, times(1))
        .findAllByMemberTravelType(any(MemberTravelType.class));
    verify(recommendRepository, times(1))
        .findNatureRecommendPostDto(1L, locale, travelType);
    verify(recommendRepository, times(1))
        .findExperienceRecommendPostDto(2L, locale, travelType);
  }

  @DisplayName("추천 게시물 반환 성공 - NONE 타입일 때")
  @Test
  void getRecommendPostsSuccess2() {
    // given
    TravelType travelType = TravelType.NONE;
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);
    Locale locale = memberInfoDto.getLanguage().getLocale();
    MemberTravelType randomMemberTravelType = new MemberTravelType(TravelType.GAMGYUL_COCKTAIL);
    TravelType randomTravelType = randomMemberTravelType.getTravelType();

    doReturn(randomMemberTravelType).when(memberTravelTypeRepository)
        .findByTravelType(argThat(new ExcludeNoneTravelTypeMatcher()));
    doReturn(recommendList(randomTravelType)).when(recommendRepository)
        .findAllByMemberTravelType(randomMemberTravelType);
    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
        .findNatureRecommendPostDto(1L, locale, randomTravelType);
    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
        .findExperienceRecommendPostDto(2L, locale, randomTravelType);

    // when
    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);

    // then
    assertThat(resultDto.size()).isEqualTo(2);

    // verify
    verify(memberTravelTypeRepository, times(1))
        .findByTravelType(argThat(new ExcludeNoneTravelTypeMatcher()));
    verify(recommendRepository, times(1))
        .findAllByMemberTravelType(any(MemberTravelType.class));
    verify(recommendRepository, times(1))
        .findNatureRecommendPostDto(1L, locale, randomTravelType);
    verify(recommendRepository, times(1))
        .findExperienceRecommendPostDto(2L, locale, randomTravelType);
  }

  @DisplayName("추천 게시물 반환 실패 - 추천 게시물이 2개 이하일 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = Mode.EXCLUDE)
  void getRecommendPostsFail(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);

    doReturn(null).when(recommendRepository)
        .findAllByMemberTravelType(any(MemberTravelType.class));

    // when
    RuntimeException runtimeException = assertThrows(RuntimeException.class,
        () -> memberTypeService.getRecommendPostsByType(memberInfoDto));

    // then
    assertThat(runtimeException.getMessage())
        .isEqualTo(travelType.name() + "에 해당하는 추천 게시물이 없거나 너무 적습니다.");
  }

  private MemberInfoDto createMemberInfoDto(Locale locale, TravelType travelType) {
    Language language = Language.builder()
        .locale(locale)
        .build();
    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(travelType)
        .build();
    Member member = Member.builder()
        .language(language)
        .memberTravelType(memberTravelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private List<Recommend> recommendList(TravelType travelType) {
    Category nature = Category.builder()
        .content(CategoryContent.NATURE)
        .build();
    Category experience = Category.builder()
        .content(CategoryContent.EXPERIENCE)
        .build();
    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(travelType)
        .build();

    return Arrays.asList(
        new Recommend(memberTravelType, 1L, nature),
        new Recommend(memberTravelType, 2L, experience)
    );
  }

  static class ExcludeNoneTravelTypeMatcher implements ArgumentMatcher<TravelType> {

    @Override
    public boolean matches(TravelType argument) {
      return argument != TravelType.NONE;
    }
  }
}