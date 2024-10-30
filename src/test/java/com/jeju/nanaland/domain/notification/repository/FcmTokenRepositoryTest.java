package com.jeju.nanaland.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FcmTokenRepositoryTest {

  @Autowired
  TestEntityManager em;

  @Autowired
  FcmTokenRepository fcmTokenRepository;

  @ParameterizedTest
  @EnumSource(value = Language.class)
  void findAllByMemberLanguageTest(Language language) {
    // given
    // 언어 별로 다른 개수의 fcm 토큰 생성
    HashMap<Language, Integer> map = new HashMap<>();
    map.put(Language.KOREAN, 1);
    map.put(Language.ENGLISH, 2);
    map.put(Language.CHINESE, 3);
    map.put(Language.MALAYSIA, 4);
    map.put(Language.VIETNAMESE, 5);
    for (Language lan : map.keySet()) {
      List<Member> memberList = initMemberListWithLanguage(lan, map.get(lan));
      for (Member member : memberList) {
        initFcmToken(member);
      }
    }

    // when
    List<FcmToken> result = fcmTokenRepository.findAllByMemberLanguage(language);

    // then
    assertThat(result.size()).isEqualTo(map.get(language));
    assertThat(result)
        .extracting(fcmToken -> fcmToken.getMember().getLanguage())
        .containsOnly(language);
  }

  List<Member> initMemberListWithLanguage(Language language, int size) {
    List<Member> memberList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Member member = Member.builder()
          .language(language)
          .provider(Provider.GUEST)
          .providerId("testProviderId")
          .email("test@naver.com")
          .nickname(UUID.randomUUID().toString())
          .travelType(TravelType.NONE)
          .profileImageFile(initImageFile())
          .build();

      em.persist(member);
      memberList.add(member);
    }

    return memberList;
  }

  void initFcmToken(Member member) {
    FcmToken fcmToken = FcmToken.builder()
        .member(member)
        .token(UUID.randomUUID().toString())
        .timestamp(LocalDateTime.now())
        .build();
    em.persist(fcmToken);
  }

  ImageFile initImageFile() {
    ImageFile imageFile = ImageFile.builder()
        .originUrl("originUrl")
        .thumbnailUrl("thumbnailUrl")
        .build();
    em.persist(imageFile);
    return imageFile;
  }
}
