package com.jeju.nanaland.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // api 만들고 수정하기
  BAD_REQUEST_EXCEPTION(BAD_REQUEST, "잘못된 요청입니다."),
  REQUEST_VALIDATION_EXCEPTION(BAD_REQUEST, "입력 형태가 잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버측 에러입니다."),
  UNAUTHORIZED_USER(UNAUTHORIZED, "access token이 존재하지 않습니다."),
  EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
  UNSUPPORTED_FILE_FORMAT(UNSUPPORTED_MEDIA_TYPE, "적절하지 않은 확장자의 파일입니다."),
  ACCESS_DENIED(UNAUTHORIZED, "접근 권한이 없습니다."),
  INVALID_TOKEN(UNAUTHORIZED, "토큰이 유효하지 않습니다."),

  NOT_FOUND_EXCEPTION(NOT_FOUND, "존재하는 데이터를 찾을 수 없습니다"),

  MEMBER_NOT_FOUND(NOT_FOUND, "존재하는 회원을 찾을 수 없습니다."),

  CONFLICT_DATA(CONFLICT, "이미 존재하는 데이터입니다."),
  MEMBER_DUPLICATE(CONFLICT, "이미 가입된 계정이 존재합니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
