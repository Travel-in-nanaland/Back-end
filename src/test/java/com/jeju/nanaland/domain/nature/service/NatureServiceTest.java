package com.jeju.nanaland.domain.nature.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class NatureServiceTest {

  MemberInfoDto memberInfoDto;
  @InjectMocks
  private NatureService natureService;
  @Mock
  private NatureRepository natureRepository;
  @Mock
  private FavoriteService favoriteService;

  @BeforeEach
  void setUp() {
    memberInfoDto = createMemberInfoDto();
  }

  private MemberInfoDto createMemberInfoDto() {
    Language language = Language.builder()
        .locale(Locale.KOREAN)
        .build();
    MemberTravelType memberTravelType = MemberTravelType.builder()
        .travelType(TravelType.NONE)
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

  private Page<NatureThumbnail> getNatureThumbnailList() {
    List<NatureThumbnail> natureThumbnails = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      natureThumbnails.add(
          NatureThumbnail.builder()
              .id((long) i)
              .title("nature title " + i)
              .addressTag("제주시")
              .build());
    }

    return new PageImpl<>(natureThumbnails, PageRequest.of(0, 2), 10);
  }

  @Test
  @DisplayName("7대 자연 썸네일 리스트 조회 성공")
  void getNatureListSuccess() {
    // given
    Page<NatureThumbnail> natureThumbnailList = getNatureThumbnailList();

    doReturn(natureThumbnailList).when(natureRepository)
        .findNatureThumbnails(any(Locale.class), anyList(), any(), any());
    doReturn(new ArrayList<>()).when(favoriteService)
        .getFavoritePostIdsWithMemberAndCategory(any(), any(CategoryContent.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        0, 2);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getData()).hasSize(2);
    assertThat(result.getData().get(0).getTitle()).isEqualTo("nature title 1");
  }

  @Test
  @DisplayName("7대 자연 썸네일 리스트 조회 성공 - 빈 값인 경우")
  void getNatureListSuccessEmpty() {
    // given
    int pageNumber = 0;
    int pageSize = 2;
    Page<NatureThumbnail> emptyNatureThumbnailList = new PageImpl<>(new ArrayList<>(),
        PageRequest.of(pageNumber, pageSize), 0);

    doReturn(emptyNatureThumbnailList).when(natureRepository)
        .findNatureThumbnails(any(Locale.class), anyList(), any(), any());
    doReturn(new ArrayList<>()).when(favoriteService)
        .getFavoritePostIdsWithMemberAndCategory(any(), any(CategoryContent.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        pageNumber, pageSize);

    // then
    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getData()).isEmpty();
  }

  @Test
  @DisplayName("7대 자연 썸네일 리스트 조회 성공 - 좋아요 여부 확인")
  void getNatureListSuccessWithFavorites() {
    // given
    Page<NatureThumbnail> natureThumbnailList = getNatureThumbnailList();
    List<Long> favoriteIds = List.of(1L);

    doReturn(natureThumbnailList).when(natureRepository)
        .findNatureThumbnails(any(Locale.class), anyList(), any(), any());
    doReturn(favoriteIds).when(favoriteService)
        .getFavoritePostIdsWithMemberAndCategory(any(), any(CategoryContent.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        0, 2);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getData()).hasSize(2);
    assertThat(result.getData().get(0).isFavorite()).isTrue();
    assertThat(result.getData().get(1).isFavorite()).isFalse();
  }

  @Test
  void getNatureDetail() {
  }
}