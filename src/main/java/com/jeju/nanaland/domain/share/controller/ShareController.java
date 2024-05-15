package com.jeju.nanaland.domain.share.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/share")
public class ShareController {

  @GetMapping("")
  public String test(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);
    return "deeplink";
  }
}
