package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
}