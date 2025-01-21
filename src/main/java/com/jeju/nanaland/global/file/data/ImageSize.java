package com.jeju.nanaland.global.file.data;

import java.util.Arrays;

public enum ImageSize {
  MEMBER_PROFILE("member_profile", 50, 50),
  REVIEW("review", 70, 70),
  TEST("test", 100, 100);

  private final String directory;
  private final int width;
  private final int height;

  ImageSize(String directory, int width, int height) {
    this.directory = directory;
    this.width = width;
    this.height = height;
  }

  public static String getDimension(String fileKey) {
    String directory = fileKey.substring(0, fileKey.lastIndexOf('/'));

    return Arrays.stream(values())
        .filter(size -> size.directory.equals(directory.replaceFirst("^/", "")))
        .findFirst()
        .map(size -> String.format("?w=%d&h=%d", size.width, size.height))
        .orElse(null);
  }
}