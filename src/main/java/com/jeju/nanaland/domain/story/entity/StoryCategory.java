package com.jeju.nanaland.domain.story.entity;

public enum StoryCategory {
  RESTAURANT("음식점"),
  CAFE_DESSERT("카페 디저트"),
  FARM_FRUITS("농장 과일"),
  NATURE("네이쳐"),
  DRINKS("드링크"),
  UNIQUE_PLAY("이색 놀이");

  private final String description;

  StoryCategory(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
