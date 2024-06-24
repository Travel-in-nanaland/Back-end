package com.jeju.nanaland.util;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class InitTestData {

  @PersistenceContext
  EntityManager em;

  /**
   * 테스트 디비에 넣을 데이터들 입니다. 나중에 ignore해서 지울 예정입니다. -test.yml -> ddl-auto : update로 변경후 테스트 돌리기 전에 이
   * 메서드만 단일로 사용해주세요!
   */
//  @Test
  @Rollback(false)
  // 롤백을 하지 않도록 설정
  void init() {
    // imageFile -> originUrl로 구분하여 조회해오므로 originUrl은 origin+숫자
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
        .dateFormat("yy-MM-dd")
        .build();
    em.persist(language1);

    Language language2 = Language.builder()
        .locale(Locale.CHINESE)
        .dateFormat("yy-MM-dd")
        .build();
    em.persist(language2);

    Language language3 = Language.builder()
        .locale(Locale.ENGLISH)
        .dateFormat("MM-dd-yy")
        .build();
    em.persist(language2);

    Language language4 = Language.builder()
        .locale(Locale.MALAYSIA)
        .dateFormat("dd-MM-yy")
        .build();
    em.persist(language4);

    Language language5 = Language.builder()
        .locale(Locale.VIETNAMESE)
        .dateFormat("dd-MM-yy")
        .build();
    em.persist(language5);

    // member -> nickname으로 구분해 조회하므로 언어+숫자 Ex) chinese3
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

    // experience
    Experience experience1 = Experience.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(experience1);

    ExperienceTrans experienceTrans1 = ExperienceTrans.builder()
        .title("이색체험 제목")
        .experience(experience1)
        .language(language1)
        .build();
    em.persist(experienceTrans1);

    // festival -> season으로 값 가져오므로 띄어쓰기 없이 데이터 입력 ex) 가을,겨울 / 봄
    Festival festival1 = Festival.builder()
        .imageFile(imageFile1)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2028, 3, 1))
        .season("봄,여름,가을,겨울")
        .build();
    em.persist(festival1);

    Festival festival2 = Festival.builder()
        .imageFile(imageFile2)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2028, 3, 2))
        .season("가을")
        .build();
    em.persist(festival2);

    Festival festival3 = Festival.builder()
        .imageFile(imageFile3)
        .onGoing(true)
        .startDate(LocalDate.of(2024, 3, 10))
        .endDate(LocalDate.of(2026, 3, 3))
        .season("겨울")
        .build();
    em.persist(festival3);

    Festival festival4 = Festival.builder()
        .imageFile(imageFile4)
        .onGoing(false)
        .startDate(LocalDate.of(2022, 3, 10))
        .endDate(LocalDate.of(2023, 3, 4))
        .season("봄,여름")
        .build();
    em.persist(festival4);

    Festival festival5 = Festival.builder()
        .imageFile(imageFile5)
        .onGoing(false)
        .startDate(LocalDate.of(2000, 4, 10))
        .endDate(LocalDate.of(2002, 3, 5))
        .season("봄,겨울")
        .build();
    em.persist(festival5);

    FestivalTrans festivalTrans1 = FestivalTrans.builder()
        .festival(festival1)
        .language(language1)
        .title("축제1")
        .address("제주특별자치도 제주시 조함해안로 525함덕해수욕장 일원")
        .addressTag("제주시")
        .build();
    em.persist(festivalTrans1);

    FestivalTrans festivalTrans2 = FestivalTrans.builder()
        .festival(festival2)
        .language(language1)
        .title("축제2")
        .address("제주특별자치도 서귀포시 중정로 22")
        .addressTag("서귀포시")
        .build();
    em.persist(festivalTrans2);

    FestivalTrans festivalTrans3 = FestivalTrans.builder()
        .festival(festival3)
        .language(language1)
        .title("축제3")
        .address("제주특별자치도 제주시 동광로 90(이도이동)")
        .addressTag("제주시")
        .build();
    em.persist(festivalTrans3);

    FestivalTrans festivalTrans4 = FestivalTrans.builder()
        .festival(festival4)
        .language(language1)
        .title("축제4")
        .address("제주특별자치도 서귀포시 표선면 녹산로 381-17")
        .addressTag("표선")
        .build();
    em.persist(festivalTrans4);

    FestivalTrans festivalTrans5 = FestivalTrans.builder()
        .festival(festival5)
        .language(language1)
        .title("축제5")
        .address("제주특별자치도 제주시 한림읍 한림로 300(한림읍)")
        .addressTag("한림")
        .build();
    em.persist(festivalTrans5);

    // market
    Market market1 = Market.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(market1);
    MarketTrans marketTrans1 = MarketTrans.builder()
        .title("전통시장 제목")
        .market(market1)
        .language(language1)
        .build();
    em.persist(marketTrans1);

    // nature
    Nature nature1 = Nature.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(nature1);

    NatureTrans natureTrans1 = NatureTrans.builder()
        .title("7대자연 제목")
        .nature(nature1)
        .language(language1)
        .build();
    em.persist(natureTrans1);

    // nana -> ver으로 구분하여 조회하므로 ver+숫
    Nana nana1 = Nana.builder()
        .version("ver1")
        .nanaTitleImageFile(imageFile1)
        .build();
    em.persist(nana1);

    Nana nana2 = Nana.builder()
        .version("ver2")
        .nanaTitleImageFile(imageFile2)
        .build();
    em.persist(nana2);

    Nana nana3 = Nana.builder()
        .version("ver3")
        .nanaTitleImageFile(imageFile3)
        .build();
    em.persist(nana3);

    Nana nana4 = Nana.builder()
        .version("ver4")
        .nanaTitleImageFile(imageFile4)
        .build();
    em.persist(nana4);

    Nana nana5 = Nana.builder()
        .version("ver5")
        .nanaTitleImageFile(imageFile5)
        .build();
    em.persist(nana5);

    NanaTitle nanaTitle1 = NanaTitle.builder()
        .notice("notice1")

        .language(language1)
        .nana(nana1)
        .build();
    em.persist(nanaTitle1);

    NanaTitle nanaTitle2 = NanaTitle.builder()
        .notice("notice2")
        .heading("keyword")
        .language(language1)
        .nana(nana2)
        .build();
    em.persist(nanaTitle2);

    NanaTitle nanaTitle3 = NanaTitle.builder()
        .notice("notice3")
        .language(language1)
        .nana(nana3)
        .build();
    em.persist(nanaTitle3);

    NanaTitle nanaTitle4 = NanaTitle.builder()
        .notice("notice4")
        .language(language1)
        .nana(nana4)
        .build();
    em.persist(nanaTitle4);

    NanaTitle nanaTitle5 = NanaTitle.builder()
        .notice("notice5")
        .language(language1)
        .nana(nana5)
        .build();
    em.persist(nanaTitle5);

    NanaContent nanaContent1 = NanaContent.builder()
        .subTitle("subtitle1")
        .nanaTitle(nanaTitle1)
        .content("content")
        .number(1)
        .title("title")
        .build();
    em.persist(nanaContent1);

    NanaContent nanaContent2 = NanaContent.builder()
        .subTitle("subtitle2")
        .nanaTitle(nanaTitle1)
        .content("content2")
        .number(2)
        .title("title2")
        .build();
    em.persist(nanaContent2);

    NanaContent nanaContent3 = NanaContent.builder()
        .subTitle("subtitle3")
        .nanaTitle(nanaTitle1)
        .content("content3")
        .number(3)
        .title("title3")
        .build();
    em.persist(nanaContent3);
  }
}
