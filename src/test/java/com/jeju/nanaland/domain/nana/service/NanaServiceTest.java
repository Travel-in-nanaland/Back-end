package com.jeju.nanaland.domain.nana.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaContentImage;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class NanaServiceTest {

  @Mock
  private NanaRepository nanaRepository;
  @Mock
  private NanaTitleRepository nanaTitleRepository;
  @Mock
  private NanaContentRepository nanaContentRepository;
  @Mock
  private FavoriteService favoriteService;
  @Mock
  private HashtagRepository hashtagRepository;
  @InjectMocks
  private NanaService nanaService;


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
    List<NanaThumbnail> nanaThumbnailList = List.of(
        NanaThumbnail.builder()
            .thumbnailUrl(nana1.getNanaTitleImageFile().getThumbnailUrl())
            .subHeading(nanaTitle1.getSubHeading())
            .heading(nanaTitle1.getHeading())
            .version(nana1.getVersion())
            .build(),
        NanaThumbnail.builder()
            .thumbnailUrl(nana2.getNanaTitleImageFile().getThumbnailUrl())
            .subHeading(nanaTitle2.getSubHeading())
            .heading(nanaTitle2.getHeading())
            .version(nana2.getVersion())
            .build(),
        NanaThumbnail.builder()
            .thumbnailUrl(nana3.getNanaTitleImageFile().getThumbnailUrl())
            .subHeading(nanaTitle3.getSubHeading())
            .heading(nanaTitle3.getHeading())
            .version(nana3.getVersion())
            .build());
    Page<NanaThumbnail> nanaThumbnails = new PageImpl<>(nanaThumbnailList, pageable,
        nanaThumbnailList.size());

    when(nanaRepository.findAllNanaThumbnailDto(language, pageable)).thenReturn(
        nanaThumbnails);

    // When
    NanaThumbnailDto nanaThumbnails1 = nanaService.getNanaThumbnails(Language.KOREAN, 0, 10);

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
    List<NanaContentImage> nanaContentImage = createNanaContentImage(
        List.of(createImageFile(1), createImageFile(2), createImageFile(3)), nana);
    nana.updateNanaContentImageList(nanaContentImage);
    Category category = Category.NANA;

    when(nanaRepository.findNanaById(anyLong())).thenReturn(Optional.of(nana));
    when(nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, language)).thenReturn(
        Optional.of(nanaTitle));
    when(nanaContentRepository.findAllByNanaTitleOrderByPriority(nanaTitle)).thenReturn(
        nanaContentList);
    when(favoriteService.isPostInFavorite(memberInfoDto.getMember(), Category.NANA,
        nanaTitle.getNana().getId())).thenReturn(true);

    // When
    NanaDetailDto nanaDetail = nanaService.getNanaDetail(memberInfoDto, 1L, false);
    List<NanaDetail> nanaDetails = nanaDetail.getNanaDetails();

    // Then
    int[] numberList = {nanaDetails.get(0).number, nanaDetails.get(1).number,
        nanaDetails.get(2).number};
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
        .nanaTitleImageFile(imageFile)
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
            .number(1)
            .title("title")
            .infoList(Set.of(
                NanaAdditionalInfo.builder()
                    .description("description1")
                    .infoType(InfoType.ADDRESS)
                    .build()
                , NanaAdditionalInfo.builder()
                    .description("description2")
                    .infoType(InfoType.ADDRESS)
                    .build()))
            .build(),

        NanaContent.builder()
            .subTitle("subtitle2")
            .nanaTitle(nanaTitle)
            .content("content2")
            .number(2)
            .title("title2")
            .infoList(Set.of(
                NanaAdditionalInfo.builder()
                    .description("description3")
                    .infoType(InfoType.ADDRESS)
                    .build()
                , NanaAdditionalInfo.builder()
                    .description("description4")
                    .infoType(InfoType.ADDRESS)
                    .build()))
            .build(),

        NanaContent.builder()
            .subTitle("subtitle3")
            .nanaTitle(nanaTitle)
            .content("content3")
            .number(3)
            .title("title3")
            .infoList(Set.of(
                NanaAdditionalInfo.builder()
                    .description("description5")
                    .infoType(InfoType.ADDRESS)
                    .build()
                , NanaAdditionalInfo.builder()
                    .description("description6")
                    .infoType(InfoType.ADDRESS)
                    .build()))
            .build());
  }

  List<NanaContentImage> createNanaContentImage(List<ImageFile> imageFileList, Nana nana) {
    List<NanaContentImage> nanaContentImageList = new ArrayList<>();
    int i = 1;
    for (ImageFile imageFile : imageFileList) {
      nanaContentImageList.add(NanaContentImage.builder()
          .imageFile(imageFile)
          .nana(nana)
          .number(i++)
          .build());
    }
    return nanaContentImageList;
  }
}
