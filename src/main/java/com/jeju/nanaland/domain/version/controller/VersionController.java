package com.jeju.nanaland.domain.version.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
public class VersionController {

  @GetMapping("/aos")
  public String getAosVersion() {
    return "2.0.1";
  }

  @GetMapping("/ios")
  public String getIosVersion() {
    return "2.0.1";
  }
}
