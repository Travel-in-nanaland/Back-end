package com.jeju.nanaland.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // api 만들고 수정하기
  BAD_REQUEST_EXCEPTION(BAD_REQUEST, "잘못된 요청입니다."),
  REQUEST_VALIDATION_EXCEPTION(BAD_REQUEST, "입력 형태가 잘못된 요청입니다."),
  UNAUTHORIZED_USER(UNAUTHORIZED, "access token이 존재하지 않습니다."),
  EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
