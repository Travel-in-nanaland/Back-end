package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.FavoriteThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nature.entity.Nature;
import java.util.Arrays;
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

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

  @InjectMocks
  FavoriteService favoriteService;

  @Mock
  FavoriteRepository favoriteRepository;

  @Test
  @DisplayName("찜리스트 전체 조회")
  void getAllFavoriteListTest() {
    // given
    MemberInfoDto memberInfoDto = initMemberInfoDto(Locale.KOREAN, TravelType.NONE);
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(0, 12);
    List<Favorite> favoriteList = Arrays.asList(
        initNatureFavorite(member),
        initMarketFavorite(member),
        initNanaFavorite(member)
    );
    Page<Favorite> favoritePage = new PageImpl<>(favoriteList);

    doReturn(favoritePage).when(favoriteRepository)
        .findAllByMemberOrderByCreatedAtDesc(memberInfoDto.getMember(), pageable);

    // when
    FavoriteThumbnailDto result = favoriteService.getAllFavoriteList(memberInfoDto, 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(3);
    System.out.println(result.getData());
  }

  private MemberInfoDto initMemberInfoDto(Locale locale, TravelType travelType) {
    Language language = Language.builder()
        .locale(locale)
        .build();
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private Favorite initNatureFavorite(Member member) {
    Nature nature = Nature.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("nature origin", "nature thumbnail"))
        .build();
    Category natureCategory = Category.builder()
        .content(CategoryContent.NATURE)
        .build();

    return Favorite.builder()
        .post(nature)
        .member(member)
        .category(natureCategory)
        .build();
  }

  private Favorite initFestivalFavorite(Member member) {
    Festival festival = Festival.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("festival origin", "festival thumbnail"))
        .build();
    Category festivalCategory = Category.builder()
        .content(CategoryContent.FESTIVAL)
        .build();

    return Favorite.builder()
        .post(festival)
        .member(member)
        .category(festivalCategory)
        .build();
  }

  private Favorite initMarketFavorite(Member member) {
    Market market = Market.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("market origin", "market thumbnail"))
        .build();
    Category marketCategory = Category.builder()
        .content(CategoryContent.MARKET)
        .build();

    return Favorite.builder()
        .post(market)
        .member(member)
        .category(marketCategory)
        .build();
  }

  private Favorite initExperienceFavorite(Member member) {
    Experience experience = Experience.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("experience origin", "experience thumbnail"))
        .build();
    Category experienceCategory = Category.builder()
        .content(CategoryContent.EXPERIENCE)
        .build();

    return Favorite.builder()
        .post(experience)
        .member(member)
        .category(experienceCategory)
        .build();
  }

  private Favorite initNanaFavorite(Member member) {
    Nana nana = Nana.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("nana origin", "nana thumbnail"))
        .build();
    Category nanaCategory = Category.builder()
        .content(CategoryContent.NANA)
        .build();

    return Favorite.builder()
        .post(nana)
        .member(member)
        .category(nanaCategory)
        .build();
  }
}