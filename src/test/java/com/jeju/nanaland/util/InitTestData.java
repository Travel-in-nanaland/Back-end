package com.jeju.nanaland.util;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class InitTestData {

  @PersistenceContext
  EntityManager em;

  //  @Test
  @Rollback(false)
  // 롤백을 하지 않도록 설정
  void init() {
    // imageFile
    ImageFile imageFile1 = ImageFile.builder()
        .originUrl("origin1")
        .thumbnailUrl("thumbnail1")
        .build();
    em.persist(imageFile1);

    ImageFile imageFile2 = ImageFile.builder()
        .originUrl("origin2")
        .thumbnailUrl("thumbnail2")
        .build();
    em.persist(imageFile2);

    ImageFile imageFile3 = ImageFile.builder()
        .originUrl("origin3")
        .thumbnailUrl("thumbnail3")
        .build();
    em.persist(imageFile3);

    ImageFile imageFile4 = ImageFile.builder()
        .originUrl("origin4")
        .thumbnailUrl("thumbnail4")
        .build();
    em.persist(imageFile4);

    ImageFile imageFile5 = ImageFile.builder()
        .originUrl("origin5")
        .thumbnailUrl("thumbnail5")
        .build();
    em.persist(imageFile5);

    // language
    Language language1 = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language1);

    Language language2 = Language.builder()
        .locale(Locale.CHINESE)
        .dateFormat("yyyy-MM-dd")
        .build();
    em.persist(language2);

    // member
    Member member1 = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId("123456789")
        .nickname("korean1")
        .language(language1)
        .profileImageFile(imageFile1)
        .build();
    em.persist(member1);

    Member member2 = Member.builder()
        .email("te1st@naver.com")
        .provider(Provider.KAKAO)
        .providerId("12789")
        .nickname("korean2")
        .language(language1)
        .profileImageFile(imageFile2)
        .build();
    em.persist(member2);

    Member member3 = Member.builder()
        .email("tesaaast@naver.com")
        .provider(Provider.KAKAO)
        .providerId("1236789")
        .nickname("chinese1")
        .language(language2)
        .profileImageFile(imageFile3)
        .build();
    em.persist(member3);

    // category
    Category category1 = Category.builder()
        .content(CategoryContent.NANA)
        .build();
    em.persist(category1);

    Category category2 = Category.builder()
        .content(CategoryContent.NANA_CONTENT)
        .build();
    em.persist(category2);

    Category category3 = Category.builder()
        .content(CategoryContent.EXPERIENCE)
        .build();
    em.persist(category3);

    Category category4 = Category.builder()
        .content(CategoryContent.FESTIVAL)
        .build();
    em.persist(category4);

    Category category5 = Category.builder()
        .content(CategoryContent.NATURE)
        .build();
    em.persist(category5);

    Category category6 = Category.builder()
        .content(CategoryContent.MARKET)
        .build();
    em.persist(category6);

    // memberTravelType
    MemberTravelType memberTravelType1 = MemberTravelType.builder()
        .travelType(TravelType.NONE)
        .build();
    em.persist(memberTravelType1);

    MemberTravelType memberTravelType2 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_ICECREAM)
        .build();
    em.persist(memberTravelType2);

    MemberTravelType memberTravelType3 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_RICECAKE)
        .build();
    em.persist(memberTravelType3);

    MemberTravelType memberTravelType4 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL)
        .build();
    em.persist(memberTravelType4);

    MemberTravelType memberTravelType5 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_CIDER)
        .build();
    em.persist(memberTravelType5);

    MemberTravelType memberTravelType6 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_AFFOKATO)
        .build();
    em.persist(memberTravelType6);

    MemberTravelType memberTravelType7 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_HANGWA)
        .build();
    em.persist(memberTravelType7);

    MemberTravelType memberTravelType8 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_JUICE)
        .build();
    em.persist(memberTravelType8);

    MemberTravelType memberTravelType9 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_CHOCOLATE)
        .build();
    em.persist(memberTravelType9);

    MemberTravelType memberTravelType10 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_COCKTAIL)
        .build();
    em.persist(memberTravelType10);

    MemberTravelType memberTravelType11 = MemberTravelType.builder()
        .travelType(TravelType.TANGERINE_PEEL_TEA)
        .build();
    em.persist(memberTravelType11);

    MemberTravelType memberTravelType12 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_YOGURT)
        .build();
    em.persist(memberTravelType12);

    MemberTravelType memberTravelType13 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_FLATCCINO)
        .build();
    em.persist(memberTravelType13);

    MemberTravelType memberTravelType14 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_LATTE)
        .build();
    em.persist(memberTravelType14);

    MemberTravelType memberTravelType15 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_SIKHYE)
        .build();
    em.persist(memberTravelType15);

    MemberTravelType memberTravelType16 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_ADE)
        .build();
    em.persist(memberTravelType16);

    MemberTravelType memberTravelType17 = MemberTravelType.builder()
        .travelType(TravelType.GAMGYUL_BUBBLE_TEA)
        .build();
    em.persist(memberTravelType17);


  }
}
