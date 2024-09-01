package com.jeju.nanaland.domain.admin.controller;

import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notification")
public class AdminNotificationController {

  @GetMapping("")
  public String notification(Model model) {
    model.addAttribute("notificationDto", new NotificationDto());
    return "/admin/admin-notification-to-all.html";
  }
}
