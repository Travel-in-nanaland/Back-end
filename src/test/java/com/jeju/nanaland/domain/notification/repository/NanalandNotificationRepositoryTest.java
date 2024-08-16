package com.jeju.nanaland.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.entity.MemberNotification;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NanalandNotificationRepositoryTest {

  @Autowired
  TestEntityManager em;

  @Autowired
  NanalandNotificationRepository nanalandNotificationRepository;

  @Test
  @DisplayName("Member 객체를 통한 알림 조회 쿼리 테스트")
  void findAllNotificationByMemberTest() {
    // given
    // 알림 SIZE 개 생성
    int SIZE = 5;
    Member member = initMember(Language.KOREAN);
    initNanalandNotificationList(member, SIZE);
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<NanalandNotification> result = nanalandNotificationRepository.findAllNotificationByMember(
        member, pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(SIZE);
  }

  @Test
  @DisplayName("MemberNotificationCompose 조회 테스트")
  void findAllMemberNotificationComposeTest() {
    // given
    // 한국 유저 알림 KR_SIZE 개, 영어 유저 알림 EN_SIZE 개 생성
    int KR_SIZE = 5;
    int EN_SIZE = 3;
    Member koreanMember = initMember(Language.KOREAN);
    initNanalandNotificationList(koreanMember, KR_SIZE);
    Member englishMember = initMember(Language.ENGLISH);
    initNanalandNotificationList(englishMember, EN_SIZE);

    // when
    List<MemberNotificationCompose> result =
        nanalandNotificationRepository.findAllMemberNotificationCompose();

    // then
    assertThat(result.size()).isEqualTo(KR_SIZE + EN_SIZE);
    assertThat(result)
        .filteredOn(memberNotificationCompose ->
            memberNotificationCompose.getMemberId().equals(koreanMember.getId()))
        .size().isEqualTo(KR_SIZE);
    assertThat(result)
        .filteredOn(memberNotificationCompose ->
            memberNotificationCompose.getMemberId().equals(englishMember.getId()))
        .size().isEqualTo(EN_SIZE);
  }

  @Test
  @DisplayName("알림 정보를 통한 조회")
  void findByNotificationInfoTest() {
    // given
    // 알림 SIZE 개 생성
    int SIZE = 5;
    Member member = initMember(Language.KOREAN);
    List<NanalandNotification> notifications = initNanalandNotificationList(member, SIZE);

    // when
    // 첫번째 알림 조회
    Optional<NanalandNotification> result = nanalandNotificationRepository.findByNotificationInfo(
        notifications.get(0).getNotificationCategory(),
        notifications.get(0).getContentId(),
        notifications.get(0).getTitle(),
        notifications.get(0).getContent());

    // then
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get()).isEqualTo(notifications.get(0));
  }

  Member initMember(Language language) {

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
    return member;
  }

  ImageFile initImageFile() {

    ImageFile imageFile = ImageFile.builder()
        .originUrl("originUrl")
        .thumbnailUrl("thumbnailUrl")
        .build();
    em.persist(imageFile);
    return imageFile;
  }

  List<NanalandNotification> initNanalandNotificationList(Member member, int size) {

    List<NanalandNotification> nanalandNotificationList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      NanalandNotification nanalandNotification = NanalandNotification.builder()
          .notificationCategory(NotificationCategory.RESTAURANT)
          .contentId(1L)
          .title("test title")
          .content(UUID.randomUUID().toString())
          .build();
      em.persist(nanalandNotification);
      nanalandNotificationList.add(nanalandNotification);

      MemberNotification memberNotification = MemberNotification.builder()
          .memberId(member.getId())
          .nanalandNotification(nanalandNotification)
          .build();
      em.persist(memberNotification);
    }

    return nanalandNotificationList;
  }
}
