package com.jeju.nanaland.domain.nature.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NatureServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  NatureService natureService;
  @Autowired
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;

  MemberInfoDto memberInfoDto1, memberInfoDto2;
  Nature nature;
  Category category;

  @BeforeEach
  void init() {
    ImageFile imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    ImageFile imageFile2 = TestUtil.findImageFileByNumber(em, 2);

    language = TestUtil.findLanguage(em, Locale.KOREAN);

    member1 = TestUtil.findMemberByLanguage(em, language, 1);

    member2 = TestUtil.findMemberByLanguage(em, language, 2);

    memberInfoDto1 = MemberInfoDto.builder()
        .language(language)
        .member(member1)
        .build();

    memberInfoDto2 = MemberInfoDto.builder()
        .language(language)
        .member(member2)
        .build();

    nature = Nature.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(nature);

    category = TestUtil.findCategory(em, CategoryContent.NATURE);
  }
}