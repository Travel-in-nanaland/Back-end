package com.jeju.nanaland.domain.notification.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationWithTargetDto;
import com.jeju.nanaland.domain.notification.entity.FavoriteNotification;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import com.jeju.nanaland.domain.notification.repository.FavoriteNotificationRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteNotificationService {

  private final NotificationService notificationService;
  private final FavoriteNotificationRepository favoriteNotificationRepository;
  private final FavoriteRepository favoriteRepository;


  // 매일 10시에 나의 찜 알림 대상에게 알림 전송
  @Transactional
  @Scheduled(cron = "0 0 10 * * *")
  public void sendMyFavoriteNotification() {

    List<FavoriteNotification> favoriteNotifications =
        favoriteNotificationRepository.findAllFavoriteNotificationToSend();

    for (FavoriteNotification fn : favoriteNotifications) {
      Member member = fn.getMember();
      Category category = fn.getCategory();
      Long postId = fn.getPostId();
      Language language = member.getLanguage();

      ThumbnailDto thumbnailDto = getThumbnailDto(member, postId, language, category);
      String postTitle = thumbnailDto.getTitle();
      String notificationTitle = getFavoriteNotificationTitle(postTitle, language);
      String content = getFavoriteNotificationContent(language);

      NotificationWithTargetDto notificationWithTargetDto = NotificationWithTargetDto.builder()
          .memberId(member.getId())
          .notificationDto(NotificationDto.builder()
              .title(notificationTitle)
              .content(content)
              .contentId(postId)
              .category(NotificationCategory.valueOf(category.name()))
              .build())
          .build();

      notificationService.sendPushNotificationToSingleTarget(notificationWithTargetDto);
    }
  }

  public FavoriteNotification createFavoriteNotification(Favorite favorite) {
    Member member = favorite.getMember();
    Long postId = favorite.getPost().getId();
    Category category = favorite.getCategory();
    boolean isSent = false;
    String status = "ACTIVE";

    FavoriteNotification favoriteNotification = FavoriteNotification.builder()
        .member(member)
        .postId(postId)
        .category(category)
        .isSent(isSent)
        .status(status)
        .build();

    return favoriteNotificationRepository.save(favoriteNotification);
  }

  public void setFavoriteNotificationStatusActive(Favorite favorite) {

    Optional<FavoriteNotification> fnOptional = getFavoriteNotification(favorite);
    if (fnOptional.isEmpty()) {
      throw new NotFoundException("찜 알림 정보가 존재하지 않습니다.");
    }

    FavoriteNotification favoriteNotification = fnOptional.get();
    favoriteNotification.setStatusActive();
  }

  public void setFavoriteNotificationStatusInactive(Favorite favorite) {

    Optional<FavoriteNotification> fnOptional = getFavoriteNotification(favorite);
    if (fnOptional.isEmpty()) {
      throw new NotFoundException("찜 알림 정보가 존재하지 않습니다.");
    }

    FavoriteNotification favoriteNotification = fnOptional.get();
    favoriteNotification.setStatusInactive();
  }

  public Optional<FavoriteNotification> getFavoriteNotification(Favorite favorite) {
    Member member = favorite.getMember();
    Long postId = favorite.getPost().getId();
    Category category = favorite.getCategory();

    return favoriteNotificationRepository.findByMemberAndPostIdAndCategory(member, postId,
        category);
  }

  private ThumbnailDto getThumbnailDto(Member member, Long postId, Language locale,
      Category category) {
    return switch (category) {
      case NANA -> favoriteRepository.findNanaThumbnailByPostId(member, postId, locale);
      case NATURE -> favoriteRepository.findNatureThumbnailByPostId(member, postId, locale);
      case MARKET -> favoriteRepository.findMarketThumbnailByPostId(member, postId, locale);
      case EXPERIENCE -> favoriteRepository.findExperienceThumbnailByPostId(member, postId, locale);
      case FESTIVAL -> favoriteRepository.findFestivalThumbnailByPostId(member, postId, locale);
      case RESTAURANT -> favoriteRepository.findRestaurantThumbnailByPostId(member, postId, locale);
      default -> null;
    };
  }

  private String getFavoriteNotificationTitle(String postTitle, Language language) {
    return switch (language) {
      case KOREAN -> "[나의 찜] " + postTitle;
      case ENGLISH -> "[My Favorite] " + postTitle;
      case CHINESE -> "[My Favorite] " + postTitle;
      case MALAYSIA -> "[My Favorite] " + postTitle;
      case VIETNAMESE -> "[My Favorite] " + postTitle;
    };
  }

  private String getFavoriteNotificationContent(Language language) {
    return switch (language) {
      case KOREAN -> "가장 좋아요를 많이 받은 곳들을 소개해줄게요";
      case ENGLISH -> "가장 좋아요를 많이 받은 곳들을 소개해줄게요";
      case CHINESE -> "가장 좋아요를 많이 받은 곳들을 소개해줄게요";
      case MALAYSIA -> "가장 좋아요를 많이 받은 곳들을 소개해줄게요";
      case VIETNAMESE -> "가장 좋아요를 많이 받은 곳들을 소개해줄게요";
    };
  }
}
