package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import com.jeju.nanaland.domain.nature.entity.Nature;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class MemberTypeServiceTest {

  @InjectMocks
  MemberTypeService memberTypeService;

  @Mock
  RecommendRepository recommendRepository;

  @Mock
  MemberFavoriteService memberFavoriteService;

  @DisplayName("타입 수정 - NONE 타입이 아닐 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void updateTypeToNotNone(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(travelType.name());

    // when
    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);

    // then
    assertThat(memberInfoDto.getMember().getTravelType()).isEqualTo(travelType);
  }


  @DisplayName("추천 게시물 반환 - NONE 타입이 아닐 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void getRecommendPostsWithNotNone(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, travelType);
    Member member = memberInfoDto.getMember();
    Language locale = memberInfoDto.getLanguage();
    List<Recommend> recommends = recommendList(travelType);

    when(
        recommendRepository.findAllByTravelType(travelType))
        .thenReturn(recommends);
    when(recommendRepository.findNatureRecommendPostDto(null, locale, travelType))
        .thenReturn(RecommendPostDto.builder().build());
    when(recommendRepository.findExperienceRecommendPostDto(null, locale, travelType))
        .thenReturn(RecommendPostDto.builder().build());
    when(
        memberFavoriteService.isPostInFavorite(eq(member), any(Category.class), eq(null)))
        .thenReturn(true);

    // when
    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);

    // then
    assertThat(resultDto.size()).isEqualTo(2);

    // verify - 쿼리 5회
    verify(recommendRepository, times(1))
        .findAllByTravelType(travelType);
    verify(recommendRepository, times(1))
        .findNatureRecommendPostDto(null, locale, travelType);
    verify(recommendRepository, times(1))
        .findExperienceRecommendPostDto(null, locale, travelType);
    verify(memberFavoriteService, times(2))
        .isPostInFavorite(eq(member), any(Category.class), eq(null));
  }

  @DisplayName("추천 게시물 반환 - NONE 타입일 때")
  @Test
  void getRecommendPostsWithNoneType() {
    // given
    TravelType travelType = TravelType.NONE;
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, travelType);
    Member member = memberInfoDto.getMember();
    Language locale = memberInfoDto.getLanguage();
    List<Recommend> recommends = recommendList(TravelType.GAMGYUL_ICECREAM); // 랜덤으로 선택된 타입

    when(
        recommendRepository.findAllByTravelType(any(TravelType.class)))
        .thenReturn(recommends);
    when(recommendRepository.findNatureRecommendPostDto(eq(null), eq(locale),
        any(TravelType.class)))
        .thenReturn(RecommendPostDto.builder().build());
    when(recommendRepository.findExperienceRecommendPostDto(eq(null), eq(locale),
        any(TravelType.class)))
        .thenReturn(RecommendPostDto.builder().build());
    when(
        memberFavoriteService.isPostInFavorite(eq(member), any(Category.class), eq(null)))
        .thenReturn(true);

    // when
    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);

    // then
    assertThat(resultDto.size()).isEqualTo(2);

    // verify - 쿼리 5회
    verify(recommendRepository, times(1))
        .findAllByTravelType(any(TravelType.class));
    verify(recommendRepository, times(1))
        .findNatureRecommendPostDto(eq(null), eq(locale), any(TravelType.class));
    verify(recommendRepository, times(1))
        .findExperienceRecommendPostDto(eq(null), eq(locale), any(TravelType.class));
    verify(memberFavoriteService, times(2))
        .isPostInFavorite(eq(member), any(Category.class), eq(null));
  }

  @DisplayName("추천 게시물 반환 - 추천 게시물이 너무 적을 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = Mode.EXCLUDE)
  void getRecommendPostsWithNoRecommend(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, travelType);

    doReturn(null)
        .when(recommendRepository)
        .findAllByTravelType(travelType);

    // when
    RuntimeException runtimeException = assertThrows(RuntimeException.class,
        () -> memberTypeService.getRecommendPostsByType(memberInfoDto));

    // then
    assertThat(runtimeException.getMessage())
        .isEqualTo(travelType.name() + "에 해당하는 추천 게시물이 없거나 너무 적습니다.");
  }

  private MemberInfoDto createMemberInfoDto(Language language, TravelType travelType) {
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private List<Recommend> recommendList(TravelType travelType) {
    Category natureCategory = Category.NATURE;
    Category experienceCategory = Category.EXPERIENCE;
    ImageFile imageFile1 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    ImageFile imageFile2 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    Nature nature = Nature.builder()
        .firstImageFile(imageFile1)
        .priority(0L)
        .build();
    Experience experience = Experience.builder()
        .firstImageFile(imageFile1)
        .priority(0L)
        .build();

    return Arrays.asList(
        Recommend.builder()
            .travelType(travelType)
            .post(nature)
            .category(natureCategory)
            .firstImageFile(imageFile1)
            .build(),
        Recommend.builder()
            .travelType(travelType)
            .post(experience)
            .category(experienceCategory)
            .firstImageFile(imageFile2)
            .build()
    );
  }
}