package com.jeju.nanaland.domain.experience.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class ExperienceServiceTest {

  @InjectMocks
  ExperienceService experienceService;
  @Mock
  ExperienceRepository experienceRepository;
  @Mock
  MemberFavoriteService memberFavoriteService;
  @Mock
  ImageFileRepository imageFileRepository;
  @Mock
  ReviewRepository reviewRepository;

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("이색체험 preview 정보 조회")
  void getPostCardDtoTest(Language language) {
    // given
    ImageFile imageFile = createImageFile();
    Experience experience = createExperience(imageFile, ExperienceType.ACTIVITY);
    ExperienceTrans experienceTrans = createExperienceTrans(language, experience);
    PostPreviewDto postPreviewDto = PostPreviewDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(experienceTrans.getTitle())
        .id(experience.getId())
        .category(Category.EXPERIENCE.toString())
        .build();
    when(experienceRepository.findPostPreviewDto(nullable(Long.class), eq(language)))
        .thenReturn(postPreviewDto);

    // when
    PostPreviewDto result =
        experienceService.getPostPreviewDto(postPreviewDto.getId(), Category.EXPERIENCE, language);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postPreviewDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postPreviewDto.getTitle());
  }

  @Test
  @DisplayName("이색체험 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile();
    Experience experience = Experience.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(experienceRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(experience));

    // when
    Post post = experienceService.getPost(1L, Category.EXPERIENCE);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  @Test
  @DisplayName("이색체험 상세조회")
  void getExperienceDetailTest() {
    // given
    Language language = Language.KOREAN;
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Long postId = 1L;
    ExperienceCompositeDto experienceCompositeDto = ExperienceCompositeDto.builder()
        .title(UUID.randomUUID().toString())
        .build();

    doReturn(experienceCompositeDto).when(experienceRepository)
        .findExperienceCompositeDto(postId, language);
    doReturn(false).when(memberFavoriteService)
        .isPostInFavorite(memberInfoDto.getMember(), Category.EXPERIENCE, postId);
    doReturn(List.of()).when(imageFileRepository)  // 빈 이미지 리스트
        .findPostImageFiles(postId);

    // when
    ExperienceResponse.DetailDto result =
        experienceService.getExperienceDetail(memberInfoDto, postId, false);

    // then
    assertThat(result.getTitle()).isEqualTo(experienceCompositeDto.getTitle());
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("액티비티 리스트 조회")
  void getActivitiesTest(Language language) {
    // given
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Pageable pageable = PageRequest.of(0, 12);

    List<ExperienceResponse.PreviewDto> previewDtos = List.of(
        ExperienceResponse.PreviewDto.builder()
            .title(UUID.randomUUID().toString())
            .addressTag("제주시")
            .build(),
        ExperienceResponse.PreviewDto.builder()
            .title(UUID.randomUUID().toString())
            .addressTag("서귀포시")
            .build()
    );
    Page<ExperienceResponse.PreviewDto> previewPage =
        new PageImpl<>(
            previewDtos,
            pageable,
            previewDtos.size());

    doReturn(previewPage).when(experienceRepository)
        .findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(language,
            ExperienceType.ACTIVITY, List.of(), List.of(), pageable);
    doReturn(4.32).when(reviewRepository)
        .findTotalRatingAvg(Category.EXPERIENCE, null);

    // when
    ExperienceResponse.PreviewPageDto result = experienceService.getExperiencePreviews(
        memberInfoDto, ExperienceType.ACTIVITY, List.of(), List.of(), 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(previewDtos.size());
    assertThat(result.getData()).isEqualTo(previewDtos);
  }

  @ParameterizedTest
  @EnumSource(value = Language.class)
  @DisplayName("문화예술 리스트 조회")
  void getCultureAndArtsTest(Language language) {
    // given
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Pageable pageable = PageRequest.of(0, 12);

    List<ExperienceResponse.PreviewDto> previewDtos = List.of(
        ExperienceResponse.PreviewDto.builder()
            .title(UUID.randomUUID().toString())
            .addressTag("제주시")
            .build(),
        ExperienceResponse.PreviewDto.builder()
            .title(UUID.randomUUID().toString())
            .addressTag("서귀포시")
            .build()
    );
    Page<ExperienceResponse.PreviewDto> previewPage =
        new PageImpl<>(
            previewDtos,
            pageable,
            previewDtos.size());

    doReturn(previewPage).when(experienceRepository)
        .findAllExperiencePreviewDtoOrderByPriorityDescAndCreatedAtDesc(language,
            ExperienceType.CULTURE_AND_ARTS, List.of(), List.of(), pageable);
    doReturn(4.32).when(reviewRepository)
        .findTotalRatingAvg(Category.EXPERIENCE, null);

    // when
    ExperienceResponse.PreviewPageDto result = experienceService.getExperiencePreviews(
        memberInfoDto, ExperienceType.CULTURE_AND_ARTS, List.of(), List.of(), 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(previewDtos.size());
    assertThat(result.getData()).isEqualTo(previewDtos);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Experience createExperience(ImageFile imageFile, ExperienceType experienceType) {
    return Experience.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .experienceType(experienceType)
        .build();
  }

  ExperienceTrans createExperienceTrans(Language language, Experience experience) {
    return ExperienceTrans.builder()
        .experience(experience)
        .language(language)
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .build();
  }

  private MemberInfoDto getMemberInfoDto(Language language, TravelType travelType) {
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }
}