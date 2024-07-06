package com.jeju.nanaland.domain.experience.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ExperienceServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  ExperienceService experienceService;
  @Autowired
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;
  MemberInfoDto memberInfoDto1, memberInfoDto2;
  Experience experience;
  Category category;

  @BeforeEach
  void init() {
    ImageFile imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    ImageFile imageFile2 = TestUtil.findImageFileByNumber(em, 2);

    language = Language.KOREAN;

    member1 = TestUtil.findMemberByLanguage(em, language, 1);

    memberInfoDto1 = MemberInfoDto.builder()
        .language(language)
        .member(member1)
        .build();

    member2 = TestUtil.findMemberByLanguage(em, language, 2);

    memberInfoDto2 = MemberInfoDto.builder()
        .language(language)
        .member(member2)
        .build();

    experience = TestUtil.findExperienceList(em, 1).get(0);

    category = Category.EXPERIENCE;


  }
}