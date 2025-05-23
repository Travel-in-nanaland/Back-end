package com.jeju.nanaland.domain.nana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.PostViewCountService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.ContentDetailDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.DetailPageDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewPageDto;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
public class NanaServiceTest {

  @Mock
  private NanaRepository nanaRepository;
  @Mock
  private NanaTitleRepository nanaTitleRepository;
  @Mock
  private NanaContentRepository nanaContentRepository;
  @Mock
  private MemberFavoriteService memberFavoriteService;
  @Mock
  private HashtagRepository hashtagRepository;
  @Mock
  private ImageFileRepository imageFileRepository;
  @Mock
  private PostViewCountService postViewCountService;
  @InjectMocks
  private NanaService nanaService;

  @Test
  @DisplayName("나나스픽 preview 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile(1);
    Nana nana = createNana(1, imageFile);
    NanaTitle nanaTitle = createNanaTitle(1, nana, Language.KOREAN);
    PostPreviewDto postPreviewDto = PostPreviewDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(nanaTitle.getHeading())
        .id(nana.getId())
        .category(Category.NANA.toString())
        .build();
    when(nanaRepository.findPostPreviewDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postPreviewDto);

    // when
    PostPreviewDto result =
        nanaService.getPostPreviewDto(postPreviewDto.getId(), Category.NANA, Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postPreviewDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postPreviewDto.getTitle());
  }

  @Test
  @DisplayName("나나스픽 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile(1);
    Nana nana = Nana.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(nanaRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(nana));

    // when
    Post post = nanaService.getPost(1L, Category.NANA);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  @Test
  void getNanaThumbnails() {
    // Given
    Language language = Language.KOREAN;
    ImageFile imageFile = createImageFile(1);
    Member member = createMember(language, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member, language);

    Nana nana1 = createNana(1, imageFile);
    Nana nana2 = createNana(2, imageFile);
    Nana nana3 = createNana(3, imageFile);
    NanaTitle nanaTitle1 = createNanaTitle(1, nana1, language);
    NanaTitle nanaTitle2 = createNanaTitle(2, nana2, language);
    NanaTitle nanaTitle3 = createNanaTitle(3, nana3, language);

    Pageable pageable = PageRequest.of(0, 10); // 0번 페이지, 페이지 크기 10
    LocalDateTime now = LocalDateTime.now();
    List<PreviewDto> previewDtoList = List.of(
        PreviewDto.builder()
            .firstImage(new ImageFileDto(nana1.getFirstImageFile().getOriginUrl(),
                nana1.getFirstImageFile().getThumbnailUrl()))
            .subHeading(nanaTitle1.getSubHeading())
            .heading(nanaTitle1.getHeading())
            .version(nana1.getVersion())
            .createdAt(now.plusDays(1))
            .build(),
        PreviewDto.builder()
            .firstImage(new ImageFileDto(nana2.getFirstImageFile().getOriginUrl(),
                nana2.getFirstImageFile().getThumbnailUrl()))
            .subHeading(nanaTitle2.getSubHeading())
            .heading(nanaTitle2.getHeading())
            .version(nana2.getVersion())
            .createdAt(now.plusDays(2))
            .build(),
        PreviewDto.builder()
            .firstImage(new ImageFileDto(nana3.getFirstImageFile().getOriginUrl(),
                nana3.getFirstImageFile().getThumbnailUrl()))
            .subHeading(nanaTitle3.getSubHeading())
            .heading(nanaTitle3.getHeading())
            .version(nana3.getVersion())
            .createdAt(now.plusDays(3))
            .build());
    Page<PreviewDto> nanaThumbnails = new PageImpl<>(previewDtoList, pageable,
        previewDtoList.size());

    when(nanaRepository.findAllPreviewDtoOrderByCreatedAt(language, pageable)).thenReturn(
        nanaThumbnails);

    // When
    PreviewPageDto nanaThumbnails1 = nanaService.getNanaThumbnails(Language.KOREAN, 0, 10);

    // Then
    Assertions.assertThat(nanaThumbnails1.getTotalElements()).isEqualTo(3L);
  }

  @Test
  void getNanaDetail() {
    // Given
    Language language = Language.KOREAN;
    ImageFile imageFile = createImageFile(1);
    Member member = createMember(language, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member, language);
    Nana nana = createNana(1, imageFile);
    NanaTitle nanaTitle = createNanaTitle(1, nana, language);
    List<NanaContent> nanaContentList = createNanaContentList(nanaTitle);
    List<List<ImageFileDto>> nanaContentImages = createNanaContentImage();
    Category category = Category.NANA;

    when(nanaRepository.findNanaByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(nana));
    when(nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, language)).thenReturn(
        Optional.of(nanaTitle));
    when(nanaContentRepository.findAllByNanaTitleOrderByPriority(nanaTitle)).thenReturn(
        nanaContentList);
    when(imageFileRepository.findPostImageFiles(nanaContentList.get(0).getId())).thenReturn(
        nanaContentImages.get(0));
    when(imageFileRepository.findPostImageFiles(nanaContentList.get(1).getId())).thenReturn(
        nanaContentImages.get(1));
    when(imageFileRepository.findPostImageFiles(nanaContentList.get(2).getId())).thenReturn(
        nanaContentImages.get(2));
    when(memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), Category.NANA,
        nanaTitle.getNana().getId())).thenReturn(true);

    // When
    DetailPageDto nanaDetail = nanaService.getNanaDetail(memberInfoDto, 1L, false);
    List<ContentDetailDto> contentDetailDtos = nanaDetail.getNanaDetails();

    // Then
    int[] numberList = {contentDetailDtos.get(0).number, contentDetailDtos.get(1).number,
        contentDetailDtos.get(2).number};
    Assertions.assertThat(numberList).containsSequence(1, 2, 3);
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

  Nana createNana(int idx, ImageFile imageFile) {
    return Nana.builder()
        .version("ver" + idx)
        .firstImageFile(imageFile)
        .priority(0L)
        .build();
  }

  NanaTitle createNanaTitle(int idx, Nana nana, Language language) {
    return NanaTitle.builder()
        .notice("notice" + idx)
        .language(language)
        .nana(nana)
        .build();
  }

  //nanaContent 3개 생성
  List<NanaContent> createNanaContentList(NanaTitle nanaTitle) {
    return List.of(NanaContent.builder()
            .subTitle("subtitle1")
            .nanaTitle(nanaTitle)
            .content("content")
            .priority(1L)
            .title("title")

            .build(),

        NanaContent.builder()
            .subTitle("subtitle2")
            .nanaTitle(nanaTitle)
            .content("content2")
            .priority(2L)
            .title("title2")
            .build(),

        NanaContent.builder()
            .subTitle("subtitle3")
            .nanaTitle(nanaTitle)
            .content("content3")
            .priority(3L)
            .title("title3")
            .build());
  }

  List<List<ImageFileDto>> createNanaContentImage() {
    List<List<ImageFileDto>> imagesList = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      List<ImageFileDto> images = new ArrayList<>();
      for (int j = 0; j < 3; j++) {
        images.add(new ImageFileDto("origin Url" + ((i * 3) + (j + 1)),
            "thumbnail Url" + ((i * 3) + (j + 1))));
      }
      imagesList.add(images);
    }

    return imagesList;
  }
}
