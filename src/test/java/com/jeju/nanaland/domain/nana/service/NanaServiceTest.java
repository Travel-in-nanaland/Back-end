package com.jeju.nanaland.domain.nana.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NanaServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  NanaService nanaService;
  @Autowired
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;
  MemberInfoDto memberInfoDto1, memberInfoDto2;
  Nana nana;
  NanaTitle nanaTitle;
  NanaContent nanaContent1, nanaContent2, nanaContent3, nanaContent4;
  Category category, category2;
  NanaAdditionalInfo nanaAdditionalInfo1, nanaAdditionalInfo2;
  Set<NanaAdditionalInfo> infoList = new HashSet<>();

  @BeforeEach
  void init() {
    ImageFile imageFile1 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile1);

    ImageFile imageFile2 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile2);

    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language);

    member1 = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(123456789L)
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile1)
        .build();
    em.persist(member1);

    memberInfoDto1 = MemberInfoDto.builder()
        .language(language)
        .member(member1)
        .build();

    member2 = Member.builder()
        .email("test2@naver.com")
        .provider(Provider.KAKAO)
        .providerId(1234567890L)
        .nickname("nickname2")
        .language(language)
        .profileImageFile(imageFile2)
        .build();
    em.persist(member2);

    memberInfoDto2 = MemberInfoDto.builder()
        .language(language)
        .member(member2)
        .build();

    nana = Nana.builder()
        .version("version1")
        .build();
    em.persist(nana);

    nanaAdditionalInfo1 = NanaAdditionalInfo.builder()
        .infoType(InfoType.ADDRESS)
        .description("address-description")
        .build();
    em.persist(nanaAdditionalInfo1);

    nanaAdditionalInfo2 = NanaAdditionalInfo.builder()
        .infoType(InfoType.AGE)
        .description("age-description")
        .build();
    em.persist(nanaAdditionalInfo2);

    infoList.add(nanaAdditionalInfo1);
    infoList.add(nanaAdditionalInfo2);

    nanaTitle = NanaTitle.builder()
        .nana(nana)
        .notice("notice")
        .imageFile(imageFile1)
        .language(language)
        .subHeading("subHeading1")
        .heading("heading1")
        .build();
    em.persist(nanaTitle);

    nanaContent1 = NanaContent.builder()
        .nanaTitle(nanaTitle)
        .imageFile(imageFile1)
        .number(1)
        .subTitle("subtitle1")
        .title("title1")
        .content("1")
        .infoList(infoList)
        .build();
    em.persist(nanaContent1);

    nanaContent2 = NanaContent.builder()
        .nanaTitle(nanaTitle)
        .imageFile(imageFile1)
        .number(2)
        .subTitle("subtitle2")
        .title("title2")
        .content("2")
        .infoList(infoList)
        .build();
    em.persist(nanaContent2);

    nanaContent3 = NanaContent.builder()
        .nanaTitle(nanaTitle)
        .imageFile(imageFile1)
        .number(2)
        .subTitle("subtitle3")
        .title("title3")
        .content("3")
        .infoList(infoList)
        .build();
    em.persist(nanaContent3);

    nanaContent4 = NanaContent.builder()
        .nanaTitle(nanaTitle)
        .imageFile(imageFile1)
        .number(4)
        .subTitle("subtitle4")
        .title("title4")
        .content("4")
        .infoList(infoList)
        .build();
    em.persist(nanaContent4);

    category = Category.builder()
        .content(CategoryContent.NANA)
        .build();
    em.persist(category);

    category2 = Category.builder()
        .content(CategoryContent.NANA_CONTENT)
        .build();
    em.persist(category2);
  }

  @Test
  void getNanaDetail() {
    NanaDetailDto nanaDetail = nanaService.getNanaDetail(memberInfoDto1,
        nanaTitle.getNana().getId(), false);
    Assertions.assertThat(nanaDetail.getSubHeading()).isNotBlank();
  }

}