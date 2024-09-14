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
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceDetailDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  @Test
  @DisplayName("이색체험 preview 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile();
    Experience experience = createExperience(imageFile);
    ExperienceTrans experienceTrans = createExperienceTrans(experience);
    PostPreviewDto postPreviewDto = PostPreviewDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(experienceTrans.getTitle())
        .id(experience.getId())
        .category(Category.EXPERIENCE.toString())
        .build();
    when(experienceRepository.findPostPreviewDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postPreviewDto);

    // when
    PostPreviewDto result =
        experienceService.getPostPreviewDto(postPreviewDto.getId(), Category.MARKET,
            Language.KOREAN);

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
    Post post = experienceService.getPost(1L, Category.MARKET);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  @Test
  @DisplayName("액티비티 상세조회")
  void getActivityList() {
    // given
    ExperienceType experienceType = ExperienceType.ACTIVITY;
    Language language = Language.KOREAN;
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Long postId = 1L;
    List<Experience> experienceList = getActivityList(language, "제주시", 1);
    ExperienceCompositeDto experienceCompositeDto = ExperienceCompositeDto.builder()
        .title("액티비티 테스트 제목1")
        .build();

    doReturn(experienceCompositeDto).when(experienceRepository)
        .findCompositeDtoById(postId, language);
    doReturn(false).when(memberFavoriteService)
        .isPostInFavorite(memberInfoDto.getMember(), Category.EXPERIENCE, postId);
    doReturn(List.of()).when(imageFileRepository)  // 빈 이미지 리스트
        .findPostImageFiles(postId);

    // when
    ExperienceDetailDto result = experienceService.getExperienceDetail(memberInfoDto, postId,
        false);

    // then
    assertThat(result).extracting("title").isEqualTo("액티비티 테스트 제목1");
  }

  @Test
  @DisplayName("액티비티 리스트 조회")
  void getExperienceListTest() {
    // given
    ExperienceType experienceType = ExperienceType.ACTIVITY;
    Language language = Language.KOREAN;
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Pageable pageable = PageRequest.of(0, 12);
    List<Experience> experienceList = new ArrayList<>();
    experienceList.addAll(getActivityList(language, "제주시", 2));
    experienceList.addAll(getActivityList(language, "제주시", 3));
    List<ExperienceThumbnail> experienceThumbnailList = List.of(
        ExperienceThumbnail.builder()
            .title("title 1")
            .build(),
        ExperienceThumbnail.builder()
            .title("title 2")
            .build()
    );
    Page<ExperienceThumbnail> experienceThumbnailPage =
        new PageImpl<>(
            experienceThumbnailList,
            pageable,
            experienceThumbnailList.size());

    doReturn(experienceThumbnailPage).when(experienceRepository)
        .findExperienceThumbnails(language, ExperienceType.ACTIVITY, List.of(), List.of(),
            pageable);
    doReturn(4.32).when(reviewRepository)
        .findTotalRatingAvg(Category.EXPERIENCE, null);

    // when
    ExperienceThumbnailDto result = experienceService.getExperienceList(memberInfoDto,
        ExperienceType.ACTIVITY, List.of(), List.of(), 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
  }

  ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl(UUID.randomUUID().toString())
        .thumbnailUrl(UUID.randomUUID().toString())
        .build();
  }

  Experience createExperience(ImageFile imageFile) {
    return Experience.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
  }

  ExperienceTrans createExperienceTrans(Experience experience) {
    return ExperienceTrans.builder()
        .experience(experience)
        .language(Language.KOREAN)
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

  private List<Experience> getActivityList(Language language, String addressTag, int size) {
    List<Experience> experienceList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("originUrl" + i)
          .thumbnailUrl("thumbnailUrl" + i)
          .build();
      Experience experience = Experience.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .experienceType(ExperienceType.ACTIVITY)
          .build();
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("activity title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();

      experienceList.add(experience);
    }

    return experienceList;
  }

  private List<Experience> getCultureAndArtsList(Language language, String addressTag, int size) {
    List<Experience> cultureAndArtsList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      ImageFile imageFile = ImageFile.builder()
          .originUrl("originUrl" + i)
          .thumbnailUrl("thumbnailUrl" + i)
          .build();
      Experience experience = Experience.builder()
          .firstImageFile(imageFile)
          .priority(0L)
          .experienceType(ExperienceType.CULTURE_AND_ARTS)
          .build();
      ExperienceTrans experienceTrans = ExperienceTrans.builder()
          .experience(experience)
          .title("culture and arts title " + i)
          .language(language)
          .addressTag(addressTag)
          .build();
      cultureAndArtsList.add(experience);
    }

    return cultureAndArtsList;
  }
}