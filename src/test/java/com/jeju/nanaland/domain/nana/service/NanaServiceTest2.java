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
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
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
    Language language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    ImageFile imageFile = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();

    Member member = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(123456789L)
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile)
        .build();

    MemberInfoDto memberInfoDto = MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
    Nana nana = Nana.builder()
        .version("ver1")
        .build();
    NanaTitle nanaTitle = NanaTitle.builder()
        .notice("notice1")
        .imageFile(imageFile)
        .language(language)
        .nana(nana)
        .build();
    List<NanaContent> nanaContentList = List.of(NanaContent.builder()
            .subTitle("subtitle1")
            .nanaTitle(nanaTitle)
            .content("content")
            .number(1)
            .title("title")
            .imageFile(imageFile)
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
            .imageFile(imageFile)
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
            .imageFile(imageFile)
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
    Category category = Category.builder()
        .content(CategoryContent.NANA_CONTENT)
        .build();

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
}
