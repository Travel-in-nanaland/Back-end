package com.jeju.nanaland.domain.nature.service;

import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.PreviewDto;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class NatureServiceTest {

  MemberInfoDto memberInfoDto;
  @InjectMocks
  private NatureService natureService;
  @Mock
  private NatureRepository natureRepository;
  @Mock
  private MemberFavoriteService memberFavoriteService;
  @Mock
  private SearchService searchService;
  @Mock
  private ImageFileService imageFileService;

  @BeforeEach
  void setUp() {
    memberInfoDto = createMemberInfoDto();
  }

  @Test
  @DisplayName("7대자연 preview 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile();
    Nature nature = createNature(imageFile);
    NatureTrans natureTrans = createNatureTrans(nature);
    PostPreviewDto postPreviewDto = PostPreviewDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(natureTrans.getTitle())
        .id(nature.getId())
        .category(Category.NATURE.toString())
        .build();
    when(natureRepository.findPostPreviewDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postPreviewDto);

    // when
    PostPreviewDto result =
        natureService.getPostPreviewDto(postPreviewDto.getId(), Category.NATURE, Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postPreviewDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postPreviewDto.getTitle());
  }

  @Test
  @DisplayName("7대자연 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile();
    Nature nature = Nature.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(natureRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(nature));

    // when
    Post post = natureService.getPost(1L, Category.NATURE);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Nature createNature(ImageFile imageFile) {
    return Nature.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
  }

  NatureTrans createNatureTrans(Nature nature) {
    return NatureTrans.builder()
        .nature(nature)
        .language(Language.KOREAN)
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .build();
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

  private Page<NatureResponse.PreviewDto> createNaturePreviews(int pageSize, int totalSize) {
    List<NatureResponse.PreviewDto> naturePreviewDtos = new ArrayList<>();
    for (int i = 1; i <= pageSize; i++) {
      naturePreviewDtos.add(
          NatureResponse.PreviewDto.builder()
              .id((long) i)
              .firstImage(new ImageFileDto("originUrl", "thumbnailUrl"))
              .title("nature title")
              .addressTag("제주시")
              .build());
    }

    return new PageImpl<>(naturePreviewDtos, PageRequest.of(0, pageSize), totalSize);
  }

  private NatureCompositeDto createNatureCompositeDto() {
    return NatureCompositeDto.builder()
        .title("nature")
        .firstImage(new ImageFileDto("originUrl", "thumbnailUrl"))
        .build();
  }

  @Nested
  @DisplayName("7대 자연 프리뷰 페이징 조회 TEST")
  class GetNaturePreview {
    @Test
    @DisplayName("성공 - 기본 케이스")
    void getNaturePreviewSuccess() {
      // given: 7대자연 프리뷰 설정
      int totalSize = 10;
      int pageSize = 2;
      Page<NatureResponse.PreviewDto> naturePreviewDtos = createNaturePreviews(pageSize, totalSize);

      doReturn(naturePreviewDtos).when(natureRepository)
          .findAllNaturePreviewDtoOrderByPriority(any(Language.class), anyList(), any(), any());
      doReturn(new ArrayList<>()).when(memberFavoriteService)
          .getFavoritePostIdsWithMember(any(Member.class));

      // when: 7대자연 프리뷰 페이징 조회
      NatureResponse.PreviewPageDto result = natureService.getNaturePreview(memberInfoDto,
          new ArrayList<>(), null, 0, 2);

      // then: 7대자연 프리뷰 리스트 검증
      assertThat(result.getTotalElements()).isEqualTo(totalSize);
      assertThat(result.getData()).hasSize(pageSize);

      PreviewDto firstItem = result.getData().get(0);
      assertThat(firstItem.getTitle()).isEqualTo("nature title");
      assertThat(firstItem.getFirstImage().getOriginUrl()).isEqualTo("originUrl");
      assertThat(firstItem.getFirstImage().getThumbnailUrl()).isEqualTo("thumbnailUrl");
      assertThat(firstItem.getAddressTag()).isEqualTo("제주시");
    }

    @Test
    @DisplayName("성공 - 빈 값인 경우")
    void getNaturePreviewSuccess_emptyList() {
      // given: 7대 자연 프리뷰 리스트가 빈 값이 되도록 설정
      int pageNumber = 0;
      int pageSize = 2;
      Page<NatureResponse.PreviewDto> naturePreviewDtos = new PageImpl<>(new ArrayList<>(),
          PageRequest.of(pageNumber, pageSize), 0);

      doReturn(naturePreviewDtos).when(natureRepository)
          .findAllNaturePreviewDtoOrderByPriority(any(Language.class), anyList(), any(), any());
      doReturn(new ArrayList<>()).when(memberFavoriteService)
          .getFavoritePostIdsWithMember(any(Member.class));

      // when: 7대자연 프리뷰 페이징 조회
      NatureResponse.PreviewPageDto result = natureService.getNaturePreview(memberInfoDto,
          new ArrayList<>(), null, pageNumber, pageSize);

      // then: 7대자연 프리뷰 리스트 검증
      assertThat(result.getTotalElements()).isZero();
      assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("성공 - 좋아요 여부 확인")
    void getNaturePreviewSuccess_withFavorites() {
      // given: 7대자연 프리뷰 설정, 좋아요 ID 설정
      int totalSize = 10;
      int pageSize = 2;
      Page<NatureResponse.PreviewDto> naturePreviewDtos = createNaturePreviews(pageSize, totalSize);
      List<Long> favoriteIds = List.of(1L);

      doReturn(naturePreviewDtos).when(natureRepository)
          .findAllNaturePreviewDtoOrderByPriority(any(Language.class), anyList(), any(), any());
      doReturn(favoriteIds).when(memberFavoriteService)
          .getFavoritePostIdsWithMember(any(Member.class));

      // when: 7대자연 프리뷰 페이징 조회
      NatureResponse.PreviewPageDto result = natureService.getNaturePreview(memberInfoDto,
          new ArrayList<>(), null, 0, pageSize);

      // then: 7대자연 프리뷰 리스트 검증
      assertThat(result.getTotalElements()).isEqualTo(totalSize);
      assertThat(result.getData()).hasSize(pageSize);
      assertThat(result.getData().get(0).isFavorite()).isTrue();
      assertThat(result.getData().get(1).isFavorite()).isFalse();
    }
  }

  @Nested
  @DisplayName("7대 자연 상세 조회 TEST")
  class GetNatureDetail {
    @Test
    @DisplayName("실패 - 해당 게시물이 존재하지 않는 경우")
    void getNatureDetailFail_postNotFound() {
      // given: 7대 자연 게시물이 존재하지 않도록 설정
      doReturn(null).when(natureRepository).findNatureCompositeDto(any(), any());

      // when: 7대 자연 상세 조회
      // then: ErrorCode 검증
      assertThatThrownBy(() -> natureService.getNatureDetail(memberInfoDto, 1L, false))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(NOT_FOUND_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("성공")
    void getNatureDetailSuccess() {
      // given: 7대자연 상세 정보 설정
      NatureCompositeDto natureCompositeDto = createNatureCompositeDto();
      List<ImageFileDto> images = List.of(
          natureCompositeDto.getFirstImage(),
          new ImageFileDto("origin url 1", "thumbnail url 1"),
          new ImageFileDto("origin url 2", "thumbnail url 2")
      );

      doReturn(natureCompositeDto).when(natureRepository).findNatureCompositeDto(any(), any());
      doReturn(true).when(memberFavoriteService).isPostInFavorite(any(), any(), any());
      doReturn(images).when(imageFileService)
          .getPostImageFilesByPostIdIncludeFirstImage(1L, natureCompositeDto.getFirstImage());

      // when: 7대 자연 상세 조회 (검색이용)
      NatureResponse.DetailDto natureDetail = natureService.getNatureDetail(memberInfoDto, 1L, true);

      // then: 7대 자연 상세 정보 검증
      assertThat(natureDetail.getTitle()).isEqualTo(natureCompositeDto.getTitle());
      assertThat(natureDetail.getImages()).hasSize(images.size());
      verify(searchService).updateSearchVolumeV1(any(), any());
    }
  }
}