package com.jeju.nanaland.domain.notification.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationWithTargetDto;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteNotificationService {

  private final NotificationService notificationService;
  private final PostService postService;
  private final FavoriteRepository favoriteRepository;


  // 매일 10시에 나의 찜 알림 대상에게 알림 전송
  @Transactional
  @Scheduled(cron = "0 0 10 * * *")
  public void sendMyFavoriteNotification() {

    List<Favorite> favorites = favoriteRepository.findAllFavoriteToSendNotification();

    log.info("좋아요 알림 개수: {}", favorites.size());

    for (Favorite favorite : favorites) {
      Member member = favorite.getMember();
      Category category = favorite.getCategory();
      Long postId = favorite.getPost().getId();
      Language language = member.getLanguage();

      PostPreviewDto postPreviewDto = postService.getPostPreviewDto(postId, category, language);
      String postTitle = postPreviewDto.getTitle();
      String notificationTitle = getFavoriteNotificationTitle(postTitle, member.getNickname(),
          language);
      String content = "";

      NotificationWithTargetDto notificationWithTargetDto = NotificationWithTargetDto.builder()
          .memberId(member.getId())
          .notificationDto(NotificationDto.builder()
              .title(notificationTitle)
              .content(content)
              .contentId(postId)
              .category(NotificationCategory.valueOf(category.name()))
              .build())
          .build();

      try {
        notificationService.sendPushNotificationToSingleTarget(notificationWithTargetDto);
      } catch (NotFoundException e) {
        log.error("알림 전송 오류 발생: {}", e.getMessage());
        TransactionStatus status = TransactionAspectSupport.currentTransactionStatus();
        log.info("isRollbackOnly flag: {}", status.isRollbackOnly());
      } catch (Exception e) {
        log.error("알 수 없는 오류 발생: {}", e.getMessage());
        TransactionStatus status = TransactionAspectSupport.currentTransactionStatus();
        log.info("isRollbackOnly flag: {}", status.isRollbackOnly());
      }
      favorite.incrementNotificationCount();
    }
  }

  private String getFavoriteNotificationTitle(String postTitle, String nickname,
      Language language) {
    return switch (language) {
      case KOREAN -> getRandomTitleKR(postTitle, nickname);
      // TODO: 현재 번역본이 없어서 모두 한글 제목으로 설정
      case ENGLISH -> getRandomTitleKR(postTitle, nickname);
      case CHINESE -> getRandomTitleKR(postTitle, nickname);
      case MALAYSIA -> getRandomTitleKR(postTitle, nickname);
      case VIETNAMESE -> getRandomTitleKR(postTitle, nickname);
    };
  }

  private String getRandomTitleKR(String postTitle, String nickname) {
    List<String> titles = List.of(
        nickname + " 님! " + postTitle + "으로 떠나보는거 어때요? ✈\uFE0F",
        nickname + " 님은, 취향을 다시 보러 가보자구\uD83D\uDE0E",
        "여행이 고민이신, " + nickname + " 님! 여기 기억나세요...?\uD83D\uDCF8",
        "한 켠에 있던 나의 버킷리스트 꺼내보기\uD83D\uDCEC"
    );

    Random random = new Random();
    int randomInt = random.nextInt(titles.size());
    return titles.get(randomInt);
  }
}
