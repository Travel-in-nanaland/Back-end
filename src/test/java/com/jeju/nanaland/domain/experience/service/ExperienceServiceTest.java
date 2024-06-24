//package com.jeju.nanaland.domain.experience.service;
//
//import com.jeju.nanaland.domain.common.data.CategoryContent;
//import com.jeju.nanaland.domain.common.entity.Category;
//import com.jeju.nanaland.domain.common.entity.ImageFile;
//import com.jeju.nanaland.domain.common.entity.Language;
//import com.jeju.nanaland.domain.common.entity.Locale;
//import com.jeju.nanaland.domain.experience.entity.Experience;
//import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
//import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
//import com.jeju.nanaland.domain.member.entity.Member;
//import com.jeju.nanaland.domain.member.entity.enums.Provider;
//import com.jeju.nanaland.util.TestUtil;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@ExtendWith(MockitoExtension.class)
//class ExperienceServiceTest {
//
//  @Autowired
//  EntityManager em;
//  @Autowired
//  ExperienceService experienceService;
//  @Autowired
//  FavoriteRepository favoriteRepository;
//
//
//  Language language;
//  Member member1, member2;
//  MemberInfoDto memberInfoDto1, memberInfoDto2;
//  Experience experience;
//  Category category;
//
//  @BeforeEach
//  void init() {
//    ImageFile imageFile1 = ImageFile.builder()
//        .originUrl("origin")
//        .thumbnailUrl("thumbnail")
//        .build();
//    em.persist(imageFile1);
//
//    ImageFile imageFile2 = ImageFile.builder()
//        .originUrl("origin")
//        .thumbnailUrl("thumbnail")
//        .build();
//    em.persist(imageFile2);
//
//    language = Language.builder()
//        .locale(Locale.KOREAN)
//        .dateFormat("yyyy-MM-dd")
//        .build();
//    em.persist(language);
//
//    member1 = Member.builder()
//        .email("test@naver.com")
//        .provider(Provider.KAKAO)
//        .providerId("123456789")
//        .nickname("nickname1")
//        .language(language)
//        .profileImageFile(imageFile1)
//        .build();
//    em.persist(member1);
//
//    memberInfoDto1 = MemberInfoDto.builder()
//        .language(language)
//        .member(member1)
//        .build();
//
//    member2 = Member.builder()
//        .email("test2@naver.com")
//        .provider(Provider.KAKAO)
//        .providerId("1234567890")
//        .nickname("nickname2")
//        .language(language)
//        .profileImageFile(imageFile2)
//        .build();
//    em.persist(member2);
//
//    memberInfoDto2 = MemberInfoDto.builder()
//        .language(language)
//        .member(member2)
//        .build();
//
//    experience = Experience.builder()
//        .imageFile(imageFile1)
//        .build();
//    em.persist(experience);
//
//    category = TestUtil.findCategory(em, CategoryContent.EXPERIENCE);
//
//
//  }
//}