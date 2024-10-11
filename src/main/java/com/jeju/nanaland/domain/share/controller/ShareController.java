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

  @GetMapping("/en")
  public String shareEn(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);

    return "deeplink-en";
  }

  @GetMapping("/ko")
  public String shareKo(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);

    return "deeplink-ko";
  }

  @GetMapping("/zh")
  public String shareZh(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);

    return "deeplink-zh";
  }

  @GetMapping("/ms")
  public String shareMs(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);

    return "deeplink-ms";
  }

  @GetMapping("/vi")
  public String shareVi(Model model, String category, Long id) {
    model.addAttribute("category", category);
    model.addAttribute("id", id);

    return "deeplink-vi";
  }
}
