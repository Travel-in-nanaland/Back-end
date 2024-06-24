package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest.LikeToggleDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.AllCategoryDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.FestivalDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.NanaDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FavoriteServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  FavoriteService favoriteService;
  @Autowired
  FavoriteRepository favoriteRepository;

  ImageFile imageFile1, imageFile2, imageFile3;
  Language language;
  Member member;
  MemberInfoDto memberInfoDto;

  @BeforeEach
  void init() {
    // imageFile
    imageFile1 = TestUtil.findImageFileByNumber(em, 1);
    imageFile2 = TestUtil.findImageFileByNumber(em, 2);
    imageFile3 = TestUtil.findImageFileByNumber(em, 3);

    // language
    language = TestUtil.findLanguage(em, Locale.KOREAN);

    // member
    member = TestUtil.findMemberByLanguage(em, language, 1);

    // memberInfoDto
    memberInfoDto = MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();

  }

  @Test
  void likeToggleTest() {
    /**
     * GIVEN
     *
     * festival, nana 생성
     */
    Festival festival = Festival.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(festival);

    Nana nana = Nana.builder()
        .version("1")
        .nanaTitleImageFile(imageFile1)
        .build();
    em.persist(nana);

    NanaTitle nanaTitle = NanaTitle.builder()
        .heading("heading")
        .subHeading("subHeading")
        .language(language)
        .nana(nana)
        .build();
    em.persist(nanaTitle);

    LikeToggleDto festivalLikeToggleDto = new LikeToggleDto();
    festivalLikeToggleDto.setCategory(CategoryContent.FESTIVAL.name());
    festivalLikeToggleDto.setId(festival.getId());

    LikeToggleDto nanaLikeToggleDto = new LikeToggleDto();
    nanaLikeToggleDto.setCategory(CategoryContent.NANA.name());
    nanaLikeToggleDto.setId(nana.getId());

    /**
     * WHEN
     */
    StatusDto festivalStatusDto = favoriteService.toggleLikeStatus(memberInfoDto,
        festivalLikeToggleDto);

    StatusDto nanaStatusDto1 = favoriteService.toggleLikeStatus(memberInfoDto, nanaLikeToggleDto);
    StatusDto nanaStatusDto2 = favoriteService.toggleLikeStatus(memberInfoDto, nanaLikeToggleDto);

    /**
     * THEN
     */
    assertThat(festivalStatusDto.isFavorite()).isTrue();
    assertThat(nanaStatusDto1.isFavorite()).isTrue();
    assertThat(nanaStatusDto2.isFavorite()).isFalse();
  }

  @Test
  void favoriteListTest() {
    /**
     * GIVEN
     *
     * festival, nana 생성
     */
    Festival festival1 = Festival.builder()
        .imageFile(imageFile1)
        .season("봄")
        .build();
    em.persist(festival1);
    FestivalTrans festivalTrans1 = FestivalTrans.builder()
        .festival(festival1)
        .language(language)
        .title("축제1")
        .build();
    em.persist(festivalTrans1);
    Festival festival2 = Festival.builder()
        .imageFile(imageFile2)
        .season("여름")
        .build();
    em.persist(festival2);
    FestivalTrans festivalTrans2 = FestivalTrans.builder()
        .festival(festival2)
        .language(language)
        .title("축제2")
        .build();
    em.persist(festivalTrans2);

    Nana nana = Nana.builder()
        .version("1")
        .nanaTitleImageFile(imageFile1)
        .build();
    em.persist(nana);
    NanaTitle nanaTitle = NanaTitle.builder()
        .heading("heading")
        .subHeading("subHeading")
        .language(language)
        .nana(nana)
        .build();
    em.persist(nanaTitle);

    /**
     * WHEN
     */
    LikeToggleDto festivalLikeToggleDto = new LikeToggleDto();
    festivalLikeToggleDto.setCategory(CategoryContent.FESTIVAL.name());
    festivalLikeToggleDto.setId(festival1.getId());
    favoriteService.toggleLikeStatus(memberInfoDto, festivalLikeToggleDto);
    festivalLikeToggleDto.setId(festival2.getId());
    favoriteService.toggleLikeStatus(memberInfoDto, festivalLikeToggleDto);

    LikeToggleDto nanaLikeToggleDto = new LikeToggleDto();
    nanaLikeToggleDto.setCategory(CategoryContent.NANA.name());
    nanaLikeToggleDto.setId(nana.getId());
    favoriteService.toggleLikeStatus(memberInfoDto, nanaLikeToggleDto);

    AllCategoryDto allFavoriteList = favoriteService.getAllFavoriteList(memberInfoDto, 0, 12);
    FestivalDto festivalFavoriteList = favoriteService.getFestivalFavoriteList(memberInfoDto, 0,
        12);
    NanaDto nanaFavoriteList = favoriteService.getNanaFavoriteList(memberInfoDto, 0, 12);

    /**
     * THEN
     */
    assertThat(allFavoriteList.getTotalElements()).isEqualTo(3);
    assertThat(festivalFavoriteList.getTotalElements()).isEqualTo(2);
    assertThat(nanaFavoriteList.getTotalElements()).isEqualTo(1);

    // 최근에 좋아요한 순서대로 표시
    assertThat(festivalFavoriteList.getData()).extracting("title")
        .containsExactly("축제2", "축제1");
  }
}