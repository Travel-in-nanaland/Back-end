package com.jeju.nanaland.global.exception;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {
  // api 만들고 수정하기
  CUSTOM_SUCCESS(OK, "~~ 조회에 성공했습니다."),
  CUSTOM_CREATED_SUCCESS(CREATED, "~ 생성에 성공했습니다."),

  // member type service
  UPDATE_MEMBER_TYPE_SUCCESS(OK, "사용자 타입 업데이트에 성공했습니다."),
  GET_RECOMMENDED_POSTS_SUCCESS(OK, "사용자 추천 게시물 조회에 성공했습니다."),

  // search
  SEARCH_SUCCESS(OK, "검색에 성공했습니다."),

  // login
  LOGIN_SUCCESS(OK, "로그인에 성공했습니다."),
  ACCESS_TOKEN_SUCCESS(OK, "AccessToken이 재발급되었습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
