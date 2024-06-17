package com.jeju.nanaland.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
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
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
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
    imageFile1 = ImageFile.builder()
        .originUrl("origin1")
        .thumbnailUrl("thumbnail1")
        .build();
    imageFile2 = ImageFile.builder()
        .originUrl("origin2")
        .thumbnailUrl("thumbnail2")
        .build();
    imageFile3 = ImageFile.builder()
        .originUrl("origin3")
        .thumbnailUrl("thumbnail3")
        .build();
    em.persist(imageFile1);
    em.persist(imageFile2);
    em.persist(imageFile3);

    // language
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language);

    // member
    member = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId("123456789")
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile1)
        .build();
    em.persist(member);

    // memberInfoDto
    memberInfoDto = MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();

    // category
    Category festivalCategory = Category.builder()
        .content(CategoryContent.FESTIVAL)
        .build();
    Category natureCategory = Category.builder()
        .content(CategoryContent.NATURE)
        .build();
    Category nanaCategory = Category.builder()
        .content(CategoryContent.NANA)
        .build();

    em.persist(natureCategory);
    em.persist(festivalCategory);
    em.persist(nanaCategory);
  }

  @Test
  void likeToggleTest() {
    /**
     * GIVEN
     *
     * festival, nana 생성
     */
    Festival festival = Festival.builder()
        .firstImageFile(imageFile1)
        .priority(0L)
        .build();
    em.persist(festival);

    // post 엔티티에서 firstImageFile가 OneToOne으로 묶여있어서 위의 imageFile1과 다른 이미지를 넣어야 합니다.
    Nana nana = Nana.builder()
        .version("3")
        .firstImageFile(imageFile2)
        .nanaTitleImageFile(imageFile1)
        .priority(0L)
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
        .firstImageFile(imageFile1)
        .season("봄")
        .priority(0L)
        .build();
    em.persist(festival1);
    FestivalTrans festivalTrans1 = FestivalTrans.builder()
        .festival(festival1)
        .language(language)
        .title("축제1")
        .build();
    em.persist(festivalTrans1);
    Festival festival2 = Festival.builder()
        .firstImageFile(imageFile2)
        .season("여름")
        .priority(0L)
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
        .firstImageFile(imageFile3)
        .nanaTitleImageFile(imageFile1)
        .priority(0L)
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