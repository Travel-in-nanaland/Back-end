package com.jeju.nanaland.domain.admin.controller;

import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationWithTargetDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notification")
public class AdminNotificationController {

  @GetMapping("/all")
  public String notificationToAll(Model model) {
    model.addAttribute("notificationDto", new NotificationDto());
    return "admin/admin-notification-to-all.html";
  }

  @GetMapping("/target")
  public String notificationToTarget(Model model) {
    model.addAttribute("reqDto", new NotificationWithTargetDto());
    return "admin/admin-notification-to-target.html";
  }
}
