package com.jeju.nanaland.domain.notification.entity.eums;


public enum NotificationCategory {

  NANA(NotificationClickEvent.POST),
  EXPERIENCE(NotificationClickEvent.POST),
  FESTIVAL(NotificationClickEvent.POST),
  NATURE(NotificationClickEvent.POST),
  MARKET(NotificationClickEvent.POST),
  RESTAURANT(NotificationClickEvent.POST),
  NOTICE(NotificationClickEvent.NOTICE),
  NONE(NotificationClickEvent.NONE);

  private final NotificationClickEvent notificationClickEvent;

  NotificationCategory(NotificationClickEvent notificationClickEvent) {
    this.notificationClickEvent = notificationClickEvent;
  }

  public NotificationClickEvent getClickEvent() {
    return notificationClickEvent;
  }
}
