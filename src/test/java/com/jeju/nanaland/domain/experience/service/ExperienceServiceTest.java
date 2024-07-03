package com.jeju.nanaland.domain.experience.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceDetailDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ExperienceServiceTest {

  @InjectMocks
  ExperienceService experienceService;

  @Mock
  ExperienceRepository experienceRepository;

  @Mock
  FavoriteService favoriteService;

  @Mock
  ImageFileRepository imageFileRepository;

  @Test
  @DisplayName("액티비티 상세조회")
  void getActivityList() {
    // given
    ExperienceType experienceType = ExperienceType.ACTIVITY;
    Language language = Language.KOREAN;
    MemberInfoDto memberInfoDto = getMemberInfoDto(language, TravelType.NONE);
    Long postId = 1L;
    List<Experience> experienceList = getActivityList(language, "지상레저", "제주시", 1);
    ExperienceCompositeDto experienceCompositeDto = ExperienceCompositeDto.builder()
        .title("액티비티 테스트 제목1")
        .build();

    doReturn(experienceCompositeDto).when(experienceRepository)
        .findCompositeDtoById(postId, language);
    doReturn(false).when(favoriteService)
        .isPostInFavorite(memberInfoDto.getMember(), Category.EXPERIENCE, postId);
    doReturn(List.of()).when(imageFileRepository)  // 빈 이미지 리스트
        .findPostImageFiles(postId);

    // when
    ExperienceDetailDto result = experienceService.getExperienceDetails(memberInfoDto, postId,
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
    experienceList.addAll(getActivityList(language, "지상레저", "제주시", 2));
    experienceList.addAll(getActivityList(language, "수상레저", "제주시", 3));
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

    // when
    ExperienceThumbnailDto result = experienceService.getExperienceList(memberInfoDto,
        ExperienceType.ACTIVITY, List.of(), List.of(), 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(2);
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

  private List<Experience> getActivityList(Language language, String keyword, String addressTag,
      int size) {
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
          .keywords(keyword)
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

  private List<Experience> getCultureAndArtsList(Language language, String keyword,
      String addressTag, int size) {
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
          .keywords(keyword)
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