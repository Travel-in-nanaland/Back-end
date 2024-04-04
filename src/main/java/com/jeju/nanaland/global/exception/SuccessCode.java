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

  // member hashtag service
  UPDATE_MEMBER_TYPE_SUCCESS(OK, "사용자 타입 업데이트에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
