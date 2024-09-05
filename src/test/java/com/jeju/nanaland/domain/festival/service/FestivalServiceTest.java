package com.jeju.nanaland.domain.festival.service;

import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalDetailDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class FestivalServiceTest {

  @InjectMocks
  private FestivalService festivalService;
  @Mock
  private FestivalRepository festivalRepository;
  @Mock
  private MemberFavoriteService memberFavoriteService;
  @Mock
  private ImageFileService imageFileService;


  @Mock
  private ImageFileRepository imageFileRepository;

  @Test
  @DisplayName("축제 카드 정보 조회")
  void getPostCardDtoTest() {
    // given
    ImageFile imageFile = createImageFile(1);
    Festival festival = createFestival(imageFile);
    FestivalTrans festivalTrans = createFestivalTrans(festival);
    PostCardDto postCardDto = PostCardDto.builder()
        .firstImage(new ImageFileDto(imageFile.getOriginUrl(), imageFile.getThumbnailUrl()))
        .title(festivalTrans.getTitle())
        .id(festival.getId())
        .category(Category.FESTIVAL.toString())
        .build();
    when(festivalRepository.findPostCardDto(nullable(Long.class), eq(Language.KOREAN)))
        .thenReturn(postCardDto);

    // when
    PostCardDto result =
        festivalService.getPostCardDto(postCardDto.getId(), FESTIVAL, Language.KOREAN);

    // then
    assertThat(result.getFirstImage()).isEqualTo(postCardDto.getFirstImage());
    assertThat(result.getTitle()).isEqualTo(postCardDto.getTitle());
  }

  @Test
  @DisplayName("축제 Post 조회")
  void getPostTest() {
    // given
    ImageFile imageFile = createImageFile(1);
    Festival festival = Festival.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
    when(festivalRepository.findById(nullable(Long.class)))
        .thenReturn(Optional.ofNullable(festival));

    // when
    Post post = festivalService.getPost(1L, Category.FESTIVAL);

    // then
    assertThat(post.getFirstImageFile()).isEqualTo(imageFile);
  }

  @Test
  @DisplayName("festival 상세 조회에서 기간 국가별 요일")
  void getFestivalDetail() {
    // Given
    Language language1 = Language.KOREAN;

    Language language2 = Language.MALAYSIA;

    // 각각 일요일 / 월요일 -> 말레이시아어로 CN / T2
    LocalDate startDate = LocalDate.of(2024, 3, 10);
    LocalDate endDate = LocalDate.of(2024, 5, 27);

    ImageFile imageFile1 = createImageFile(1);
    ImageFile imageFile2 = createImageFile(2);
    Member krMember = createMember(language1, imageFile1);
    Member msMember = createMember(language2, imageFile2);
    MemberInfoDto krMemberInfoDto = createMemberInfoDto(krMember, language1);
    MemberInfoDto msMemberInfoDto = createMemberInfoDto(msMember, language2);

    FestivalCompositeDto krFestivalCompositeDto = createFestivalCompositeDto(Language.KOREAN,
        startDate, endDate);
    FestivalCompositeDto msFestivalCompositeDto = createFestivalCompositeDto(Language.MALAYSIA,
        startDate, endDate);

    when(festivalRepository.findCompositeDtoById(1L,
        krMemberInfoDto.getLanguage())).thenReturn(krFestivalCompositeDto);
    when(festivalRepository.findCompositeDtoById(1L,
        msMemberInfoDto.getLanguage())).thenReturn(msFestivalCompositeDto);
    when(memberFavoriteService.isPostInFavorite(any(), eq(FESTIVAL), anyLong()))
        .thenReturn(false);

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

  FestivalCompositeDto createFestivalCompositeDto(Language locale, LocalDate startDate,
      LocalDate endDate) {
    return new FestivalCompositeDto(1L, "url", "url", "contact", "home",
        locale, "title", "content", "address", "addressTag", "time", "intro", "fee", startDate,
        endDate,
        "봄");
  }

  Festival createFestival(ImageFile imageFile) {
    return Festival.builder()
        .priority(0L)
        .firstImageFile(imageFile)
        .build();
  }

  FestivalTrans createFestivalTrans(Festival festival) {
    return FestivalTrans.builder()
        .festival(festival)
        .language(Language.KOREAN)
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .build();
  }
}