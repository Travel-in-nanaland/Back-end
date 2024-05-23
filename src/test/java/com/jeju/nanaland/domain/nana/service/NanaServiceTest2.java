package com.jeju.nanaland.domain.nana.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.CategoryRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
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

@ExtendWith(MockitoExtension.class)
public class NanaServiceTest2 {

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
  @Mock
  private CategoryRepository categoryRepository;
  @InjectMocks
  private NanaService nanaService;

  @Test
  void getNanaDetail() {
    // Given
    Language language = createLanguage(Locale.KOREAN);
    ImageFile imageFile = createImageFile(1);
    Member member = createMember(language, imageFile);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member, language);
    Nana nana = createNana(1, imageFile);
    NanaTitle nanaTitle = createNanaTitle(1, nana, language);
    List<NanaContent> nanaContentList = createNanaContentList(nanaTitle);
    List<NanaContentImage> nanaContentImage = createNanaContentImage(
        List.of(createImageFile(1), createImageFile(2), createImageFile(3)), nana);
    nana.updateNanaContentImageList(nanaContentImage);
    Category category = createCategory(CategoryContent.NANA);

    when(nanaRepository.findNanaById(anyLong())).thenReturn(Optional.of(nana));
    when(nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, language)).thenReturn(
        Optional.of(nanaTitle));
    when(nanaContentRepository.findAllByNanaTitleOrderByNumber(nanaTitle)).thenReturn(
        nanaContentList);
    when(favoriteService.isPostInFavorite(memberInfoDto.getMember(), CategoryContent.NANA,
        nanaTitle.getNana().getId())).thenReturn(true);
    when(categoryRepository.findByContent(CategoryContent.NANA_CONTENT)).thenReturn(
        Optional.of(category));

    // When
    NanaDetailDto nanaDetail = nanaService.getNanaDetail(memberInfoDto, 1L, false);
    List<NanaDetail> nanaDetails = nanaDetail.getNanaDetails();

    // Then
    int[] numberList = {nanaDetails.get(0).number, nanaDetails.get(1).number,
        nanaDetails.get(2).number};
    Assertions.assertThat(numberList).containsSequence(1, 2, 3);
  }

  Language createLanguage(Locale locale) {
    return Language.builder()
        .locale(locale)
        .dateFormat("yyyy-MM-dd")
        .build();
  }

  Category createCategory(CategoryContent categoryContent) {
    return Category.builder()
        .content(categoryContent)
        .build();
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

//  void setData() {
//    Member member = Member.builder()
//        .email("test@naver.com")
//        .provider(Provider.KAKAO)
//        .providerId(String.valueOf(123456789L))
//        .nickname("nickname1")
//        .language(language)
//        .profileImageFile(imageFile1)
//        .build();
//
//    MemberInfoDto memberInfoDto = MemberInfoDto.builder()
//        .member(member)
//        .language(language)
//        .build();
//    Nana nana = Nana.builder()
//        .version("ver1")
//        .nanaTitleImageFile(imageFile1)
//        .build();
//    NanaTitle nanaTitle = NanaTitle.builder()
//        .notice("notice1")
//        .language(language)
//        .nana(nana)
//        .build();
//    List<NanaContent> nanaContentList = List.of(NanaContent.builder()
//            .subTitle("subtitle1")
//            .nanaTitle(nanaTitle)
//            .content("content")
//            .number(1)
//            .title("title")
//            .infoList(Set.of(
//                NanaAdditionalInfo.builder()
//                    .description("description1")
//                    .infoType(InfoType.ADDRESS)
//                    .build()
//                , NanaAdditionalInfo.builder()
//                    .description("description2")
//                    .infoType(InfoType.ADDRESS)
//                    .build()))
//            .build(),
//
//        NanaContent.builder()
//            .subTitle("subtitle2")
//            .nanaTitle(nanaTitle)
//            .content("content2")
//            .number(2)
//            .title("title2")
//            .infoList(Set.of(
//                NanaAdditionalInfo.builder()
//                    .description("description3")
//                    .infoType(InfoType.ADDRESS)
//                    .build()
//                , NanaAdditionalInfo.builder()
//                    .description("description4")
//                    .infoType(InfoType.ADDRESS)
//                    .build()))
//            .build(),
//
//        NanaContent.builder()
//            .subTitle("subtitle3")
//            .nanaTitle(nanaTitle)
//            .content("content3")
//            .number(3)
//            .title("title3")
//            .infoList(Set.of(
//                NanaAdditionalInfo.builder()
//                    .description("description5")
//                    .infoType(InfoType.ADDRESS)
//                    .build()
//                , NanaAdditionalInfo.builder()
//                    .description("description6")
//                    .infoType(InfoType.ADDRESS)
//                    .build()))
//            .build());
//
//    NanaContentImage nanaContentImage1 = NanaContentImage.builder()
//        .imageFile(imageFile1)
//        .nana(nana)
//        .number(1)
//        .build();
//
//    NanaContentImage nanaContentImage2 = NanaContentImage.builder()
//        .imageFile(imageFile2)
//        .nana(nana)
//        .number(2)
//        .build();
//
//    NanaContentImage nanaContentImage3 = NanaContentImage.builder()
//        .imageFile(imageFile3)
//        .nana(nana)
//        .number(3)
//        .build();
//
//    nana.updateNanaContentImageList(
//        List.of(nanaContentImage1, nanaContentImage2, nanaContentImage3));
//
//    Category category = Category.builder()
//        .content(CategoryContent.NANA_CONTENT)
//        .build();
//  }
}
