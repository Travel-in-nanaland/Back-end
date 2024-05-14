package com.jeju.nanaland.domain.share.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/share")
public class ShareController {

  @GetMapping("")
  public String test(String category, Long id) {
    return "deeplink";
  }
}
