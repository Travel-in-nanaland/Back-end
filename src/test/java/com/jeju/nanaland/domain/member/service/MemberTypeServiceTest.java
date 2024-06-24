package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.RecommendRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberTypeServiceTest {

  @InjectMocks
  MemberTypeService memberTypeService;

  @Mock
  RecommendRepository recommendRepository;

  @Mock
  FavoriteRepository favoriteRepository;

  @DisplayName("타입 수정 - NONE 타입이 아닐 때")
  @ParameterizedTest
  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void updateTypeToNotNone(TravelType travelType) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, TravelType.NONE);

    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(travelType.name());

    // when
    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);

    // then
    Assertions.assertThat(memberInfoDto.getMember().getTravelType())
        .isEqualTo(travelType);
  }

  //
//  @DisplayName("타입 수정 - 없는 TravelType")
//  @Test
//  void updateTypeToWrongTravelType() {
//    /**
//     * given
//     *
//     * TravelType에 GAMGYUL_ICECREAM이 존재하지만 MemberTravelType 테이블에는 없는 경우
//     */
//    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, TravelType.NONE);
//
//    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
//    updateTypeDto.setType(TravelType.GAMGYUL_ICECREAM.name());
//
//    doReturn(null).when(memberTravelTypeRepository)
//        .findByTravelType(any(TravelType.class));
//
//    // when
//    RuntimeException runtimeException = assertThrows(RuntimeException.class,
//        () -> memberTypeService.updateMemberType(memberInfoDto, updateTypeDto));
//
//    // then
//    assertThat(runtimeException.getMessage())
//        .isEqualTo(updateTypeDto.getType() + "에 해당하는 타입 정보가 없습니다.");
//  }
//
//  @DisplayName("추천 게시물 반환 - NONE 타입이 아닐 때")
//  @ParameterizedTest
//  @EnumSource(value = TravelType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
//  void getRecommendPostsWithNotNone(TravelType travelType) {
//    // given
//    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);
//    Locale locale = memberInfoDto.getLanguage().getLocale();
//    List<Recommend> recommends = recommendList(travelType);
//    doReturn(recommends).when(recommendRepository)
//        .findAllByMemberTravelType(any(MemberTravelType.class));
//    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
//        .findNatureRecommendPostDto(recommends.get(0).getPost().getId(), locale, travelType);
//    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
//        .findExperienceRecommendPostDto(recommends.get(1).getPost().getId(), locale, travelType);
//    doReturn(Optional.empty()).when(favoriteRepository)
//        .findByMemberAndCategoryAndPostId(
//            eq(memberInfoDto.getMember()),
//            any(Category.class),
//            any(Long.class));
//
//    // when
//    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);
//
//    // then
//    assertThat(resultDto.size()).isEqualTo(2);
//
//    // verify
//    verify(recommendRepository, times(1))
//        .findAllByMemberTravelType(any(MemberTravelType.class));
//    verify(recommendRepository, times(1))
//        .findNatureRecommendPostDto(1L, locale, travelType);
//    verify(recommendRepository, times(1))
//        .findExperienceRecommendPostDto(2L, locale, travelType);
//  }
//
//  @DisplayName("추천 게시물 반환 - NONE 타입일 때")
//  @Test
//  void getRecommendPostsWithNoneType() {
//    // given
//    TravelType travelType = TravelType.NONE;
//    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);
//    Locale locale = memberInfoDto.getLanguage().getLocale();
//    MemberTravelType randomMemberTravelType = new MemberTravelType(TravelType.GAMGYUL_COCKTAIL);
//    TravelType randomTravelType = randomMemberTravelType.getTravelType();
//    List<Recommend> recommends = recommendList(randomTravelType);
//
//    doReturn(randomMemberTravelType).when(memberTravelTypeRepository)
//        .findByTravelType(argThat(new ExcludeNoneTravelTypeMatcher()));
//    doReturn(recommends).when(recommendRepository)
//        .findAllByMemberTravelType(randomMemberTravelType);
//    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
//        .findNatureRecommendPostDto(recommends.get(0).getPost().getId(), locale, randomTravelType);
//    doReturn(RecommendPostDto.builder().build()).when(recommendRepository)
//        .findExperienceRecommendPostDto(recommends.get(1).getPost().getId(), locale,
//            randomTravelType);
//    doReturn(Optional.empty()).when(favoriteRepository)
//        .findByMemberAndCategoryAndPostId(
//            eq(memberInfoDto.getMember()),
//            any(Category.class),
//            any(Long.class));
//
//    // when
//    List<RecommendPostDto> resultDto = memberTypeService.getRecommendPostsByType(memberInfoDto);
//
//    // then
//    assertThat(resultDto.size()).isEqualTo(2);
//
//    // verify
//    verify(memberTravelTypeRepository, times(1))
//        .findByTravelType(argThat(new ExcludeNoneTravelTypeMatcher()));
//    verify(recommendRepository, times(1))
//        .findAllByMemberTravelType(any(MemberTravelType.class));
//    verify(recommendRepository, times(1))
//        .findNatureRecommendPostDto(1L, locale, randomTravelType);
//    verify(recommendRepository, times(1))
//        .findExperienceRecommendPostDto(2L, locale, randomTravelType);
//  }
//
//  @DisplayName("추천 게시물 반환 - 추천 게시물이 너무 적을 때")
//  @ParameterizedTest
//  @EnumSource(value = TravelType.class, names = "NONE", mode = Mode.EXCLUDE)
//  void getRecommendPostsWithNoRecommend(TravelType travelType) {
//    // given
//    MemberInfoDto memberInfoDto = createMemberInfoDto(Locale.KOREAN, travelType);
//
//    doReturn(null).when(recommendRepository)
//        .findAllByMemberTravelType(any(MemberTravelType.class));
//
//    // when
//    RuntimeException runtimeException = assertThrows(RuntimeException.class,
//        () -> memberTypeService.getRecommendPostsByType(memberInfoDto));
//
//    // then
//    assertThat(runtimeException.getMessage())
//        .isEqualTo(travelType.name() + "에 해당하는 추천 게시물이 없거나 너무 적습니다.");
//  }
//
  private MemberInfoDto createMemberInfoDto(Locale locale, TravelType travelType) {
    Language language = Language.builder()
        .locale(locale)
        .build();
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }
//
//  private List<Recommend> recommendList(TravelType travelType) {
//    Category natureCategory = Category.builder()
//        .content(CategoryContent.NATURE)
//        .build();
//    Category experienceCategory = Category.builder()
//        .content(CategoryContent.EXPERIENCE)
//        .build();
//    MemberTravelType memberTravelType = MemberTravelType.builder()
//        .travelType(travelType)
//        .build();
//    ImageFile imageFile1 = ImageFile.builder()
//        .originUrl("origin")
//        .thumbnailUrl("thumbnail")
//        .build();
//    ImageFile imageFile2 = ImageFile.builder()
//        .originUrl("origin")
//        .thumbnailUrl("thumbnail")
//        .build();
//    Nature nature = Nature.builder()
//        .firstImageFile(imageFile1)
//        .priority(0L)
//        .build();
//    Experience experience = Experience.builder()
//        .firstImageFile(imageFile1)
//        .priority(0L)
//        .build();
//
//    return Arrays.asList(
//        new Recommend(memberTravelType, nature, natureCategory),
//        new Recommend(memberTravelType, experience, experienceCategory)
//    );
//  }
//
//  static class ExcludeNoneTravelTypeMatcher implements ArgumentMatcher<TravelType> {
//
//    @Override
//    public boolean matches(TravelType argument) {
//      return argument != TravelType.NONE;
//    }
//  }
}