package com.jeju.nanaland.domain.nature.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureDetailDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnail;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
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
  @Mock
  private SearchService searchService;
  @Mock
  private ImageFileRepository imageFileRepository;

  @Mock
  private ImageFileService imageFileService;

  @BeforeEach
  void setUp() {
    memberInfoDto = createMemberInfoDto();
  }

  private MemberInfoDto createMemberInfoDto() {
    Language language = Language.KOREAN;
    Member member = Member.builder()
        .language(language)
        .travelType(TravelType.NONE)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private Page<NatureThumbnail> createNatureThumbnailList() {
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

  private NatureCompositeDto createNatureCompositeDto() {
    return NatureCompositeDto.builder()
        .title("nature 1")
        .firstImage(new ImageFileDto("originUrl", "thumbnailUrl"))
        .build();
  }

  @Test
  @DisplayName("7대 자연 썸네일 리스트 조회 성공")
  void getNatureListSuccess() {
    // given
    Page<NatureThumbnail> natureThumbnailList = createNatureThumbnailList();

    doReturn(natureThumbnailList).when(natureRepository)
        .findNatureThumbnails(any(Language.class), anyList(), any(), any());
    doReturn(new ArrayList<>()).when(favoriteService)
        .getFavoritePostIdsWithMember(any(Member.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        0, 2);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getData()).hasSize(2);
    assertThat(result.getData().get(0).getTitle()).isEqualTo("nature title 1");

    verify(natureRepository, times(1)).findNatureThumbnails(any(Language.class), anyList(), any(),
        any());
    verify(favoriteService, times(1)).getFavoritePostIdsWithMember(
        any(Member.class));
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
        .findNatureThumbnails(any(Language.class), anyList(), any(), any());
    doReturn(new ArrayList<>()).when(favoriteService)
        .getFavoritePostIdsWithMember(any(Member.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        pageNumber, pageSize);

    // then
    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getData()).isEmpty();

    verify(natureRepository, times(1)).findNatureThumbnails(any(Language.class), anyList(), any(),
        any());
    verify(favoriteService, times(1)).getFavoritePostIdsWithMember(
        any(Member.class));
  }

  @Test
  @DisplayName("7대 자연 썸네일 리스트 조회 성공 - 좋아요 여부 확인")
  void getNatureListSuccessWithFavorites() {
    // given
    Page<NatureThumbnail> natureThumbnailList = createNatureThumbnailList();
    List<Long> favoriteIds = List.of(1L);

    doReturn(natureThumbnailList).when(natureRepository)
        .findNatureThumbnails(any(Language.class), anyList(), any(), any());
    doReturn(favoriteIds).when(favoriteService)
        .getFavoritePostIdsWithMember(any(Member.class));

    // when
    NatureThumbnailDto result = natureService.getNatureList(memberInfoDto, new ArrayList<>(), null,
        0, 2);

    // then
    assertThat(result.getTotalElements()).isEqualTo(10);
    assertThat(result.getData()).hasSize(2);
    assertThat(result.getData().get(0).isFavorite()).isTrue();
    assertThat(result.getData().get(1).isFavorite()).isFalse();

    verify(natureRepository, times(1)).findNatureThumbnails(any(Language.class), anyList(), any(),
        any());
    verify(favoriteService, times(1)).getFavoritePostIdsWithMember(
        any(Member.class));
  }

  @Test
  @DisplayName("7대 자연 상세 조회 실패 - 해당 게시물이 존재하지 않는 경우")
  void getNatureDetailFail() {
    // given
    doReturn(null).when(natureRepository).findCompositeDtoById(any(), any());

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> natureService.getNatureDetail(memberInfoDto, 1L, false));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(
        ErrorCode.NOT_FOUND_EXCEPTION.getMessage());

    verify(natureRepository, times(1)).findCompositeDtoById(any(), any());
  }

  @Test
  @DisplayName("7대 자연 상세 조회 성공")
  void getNatureDetailSuccess() {
    // given
    NatureCompositeDto natureCompositeDto = createNatureCompositeDto();
    List<ImageFileDto> images = List.of(
        natureCompositeDto.getFirstImage(),
        new ImageFileDto("origin url 1", "thumbnail url 1"),
        new ImageFileDto("origin url 2", "thumbnail url 2")
    );

    doReturn(natureCompositeDto).when(natureRepository).findCompositeDtoById(any(), any());
    doReturn(true).when(favoriteService).isPostInFavorite(any(), any(), any());
    doReturn(images).when(imageFileService)
        .getPostImageFilesByPostIdIncludeFirstImage(1L, natureCompositeDto.getFirstImage());

    // when
    NatureDetailDto natureDetail = natureService.getNatureDetail(memberInfoDto, 1L, true);

    // then
    assertThat(natureDetail.getTitle()).isEqualTo("nature 1");
    assertThat(natureDetail.getImages()).hasSize(3);

    verify(natureRepository, times(1)).findCompositeDtoById(any(), any());
    verify(favoriteService, times(1)).isPostInFavorite(any(), any(), any());
    verify(imageFileService, times(1)).getPostImageFilesByPostIdIncludeFirstImage(1L,
        natureCompositeDto.getFirstImage());
    verify(searchService, times(1)).updateSearchVolumeV1(any(), any());
  }
}