package com.jeju.nanaland.domain.festival.service;

import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalDetailDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.search.service.SearchService;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

  @InjectMocks
  private FestivalService festivalService;
  @Mock
  private FestivalRepository festivalRepository;
  @Mock
  private FavoriteService favoriteService;
  @Mock
  private SearchService searchService;

  @Test
  @DisplayName("festival 상세 조회에서 기간 국가별 요일")
  void getFestivalDetail() {
    // Given
    Language language1 = Language.builder()
        .dateFormat("yy-MM-dd")
        .locale(Locale.KOREAN)
        .build();

    Language language2 = Language.builder()
        .dateFormat("dd-MM-yy")
        .locale(Locale.MALAYSIA)
        .build();

    // 각각 일요일 / 월요일 -> 말레이시아어로 CN / T2
    LocalDate startDate = LocalDate.of(2024, 3, 10);
    LocalDate endDate = LocalDate.of(2024, 5, 27);

    ImageFile imageFile1 = createImageFile(1);
    ImageFile imageFile2 = createImageFile(2);
    Member krMember = createMember(language1, imageFile1);
    Member msMember = createMember(language2, imageFile2);
    MemberInfoDto krMemberInfoDto = createMemberInfoDto(krMember, language1);
    MemberInfoDto msMemberInfoDto = createMemberInfoDto(msMember, language2);

    FestivalCompositeDto krFestivalCompositeDto = createFestivalCompositeDto(Locale.KOREAN,
        startDate, endDate);
    FestivalCompositeDto msFestivalCompositeDto = createFestivalCompositeDto(Locale.MALAYSIA,
        startDate, endDate);

    when(festivalRepository.findCompositeDtoById(1L,
        krMemberInfoDto.getLanguage().getLocale())).thenReturn(krFestivalCompositeDto);
    when(festivalRepository.findCompositeDtoById(1L,
        msMemberInfoDto.getLanguage().getLocale())).thenReturn(msFestivalCompositeDto);
    when(favoriteService.isPostInFavorite(any(), eq(FESTIVAL), anyLong())).thenReturn(false);

    // When
    FestivalDetailDto krFestivalDetail = festivalService.getFestivalDetail(krMemberInfoDto, 1L,
        false);
    FestivalDetailDto msFestivalDetail = festivalService.getFestivalDetail(msMemberInfoDto, 1L,
        false);

    // Then
    Assertions.assertThat(krFestivalDetail.getPeriod()).isEqualTo("24. 03. 10(일) ~ 24. 05. 27(월)");
    Assertions.assertThat(msFestivalDetail.getPeriod())
        .isEqualTo("10. 03. 24(CN) ~ 27. 05. 24(T2)");
  }

  ImageFile createImageFile(int idx) {
    return ImageFile.builder()
        .thumbnailUrl("thumbnail_url" + idx)
        .originUrl("origin_url" + idx)
        .build();
  }

  Member createMember(Language language, ImageFile imageFile) {
    return Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(String.valueOf(123456789L))
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile)
        .build();
  }

  MemberInfoDto createMemberInfoDto(Member member, Language language) {
    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  FestivalCompositeDto createFestivalCompositeDto(Locale locale, LocalDate startDate,
      LocalDate endDate) {
    return new FestivalCompositeDto(1L, "url", "url", "contact", "home",
        locale, "title", "content", "address", "addressTag", "time", "intro", "fee", startDate,
        endDate,
        "봄");
  }
}