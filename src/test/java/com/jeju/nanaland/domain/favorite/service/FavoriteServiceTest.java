package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
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
class FavoriteServiceTest {

  @InjectMocks
  FavoriteService favoriteService;

  @Mock
  FavoriteRepository favoriteRepository;

  @Test
  @DisplayName("찜리스트 전체 조회")
  void getAllFavoriteListTest() {
    // given
    MemberInfoDto memberInfoDto = initMemberInfoDto(Language.KOREAN, TravelType.NONE);
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(0, 12);
    List<Favorite> favoriteList = Arrays.asList(
        initNatureFavorite(member),
        initMarketFavorite(member),
        initNanaFavorite(member)
    );
    Page<Favorite> favoritePage = new PageImpl<>(favoriteList);

    doReturn(favoritePage).when(favoriteRepository)
        .findAllByMemberAndStatusActiveOrderByCreatedAtDesc(memberInfoDto.getMember(), pageable);

    // when
    FavoriteThumbnailDto result = favoriteService.getAllFavoriteList(memberInfoDto, 0, 12);

    // then
    assertThat(result.getTotalElements()).isEqualTo(3);
    System.out.println(result.getData());
  }

  private MemberInfoDto initMemberInfoDto(Language language, TravelType travelType) {
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

    return Favorite.builder()
        .post(nature)
        .member(member)
        .category(Category.NATURE)
        .build();
  }

  private Favorite initFestivalFavorite(Member member) {
    Festival festival = Festival.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("festival origin", "festival thumbnail"))
        .build();

    return Favorite.builder()
        .post(festival)
        .member(member)
        .category(Category.FESTIVAL)
        .build();
  }

  private Favorite initMarketFavorite(Member member) {
    Market market = Market.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("market origin", "market thumbnail"))
        .build();

    return Favorite.builder()
        .post(market)
        .member(member)
        .category(Category.MARKET)
        .build();
  }

  private Favorite initExperienceFavorite(Member member) {
    Experience experience = Experience.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("experience origin", "experience thumbnail"))
        .build();

    return Favorite.builder()
        .post(experience)
        .member(member)
        .category(Category.EXPERIENCE)
        .build();
  }

  private Favorite initNanaFavorite(Member member) {
    Nana nana = Nana.builder()
        .priority(0L)
        .firstImageFile(new ImageFile("nana origin", "nana thumbnail"))
        .build();

    return Favorite.builder()
        .post(nana)
        .member(member)
        .category(Category.NANA)
        .build();
  }
}