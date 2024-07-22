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
  MEMBER_CONSENT_BAD_REQUEST(BAD_REQUEST, "TERMS_OF_USE는 필수로 동의해야 합니다."),
  REVIEW_IMAGE_BAD_REQUEST(BAD_REQUEST, "리뷰 이미지는 최대 5장까지 가능합니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버측 에러입니다."),
  UNAUTHORIZED_USER(UNAUTHORIZED, "access token이 존재하지 않습니다."),
  EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
  UNSUPPORTED_FILE_FORMAT(UNSUPPORTED_MEDIA_TYPE, "적절하지 않은 확장자의 파일입니다."),
  ACCESS_DENIED(UNAUTHORIZED, "접근 권한이 없습니다."),
  INVALID_TOKEN(UNAUTHORIZED, "토큰이 유효하지 않습니다."),

  NOT_FOUND_EXCEPTION(NOT_FOUND, "존재하는 데이터를 찾을 수 없습니다"),
  MEMBER_NOT_FOUND(NOT_FOUND, "존재하는 회원을 찾을 수 없습니다."),
  NANA_NOT_FOUND(NOT_FOUND, "존재하지 않는 Nana 입니다."),
  NANA_TITLE_NOT_FOUND(NOT_FOUND, "존재하지 않는 Nana Title 입니다."),
  MEMBER_CONSENT_NOT_FOUND(NOT_FOUND, "존재하는 이용약관 동의 여부를 찾을 수 없습니다"),
  MEMBER_WTIHDRAWAL_NOT_FOUND(NOT_FOUND, "존재하는 회원의 탈퇴 상태를 찾을 수 없습니다"),
  CATEGORY_NOT_FOUND(NOT_FOUND, "존재하는 카테고리를 찾을 수 없습니다."),
  REVIEW_NOT_FOUND(NOT_FOUND, "존재하는 리뷰를 찾을 수 없습니다."),

  CONFLICT_DATA(CONFLICT, "이미 존재하는 데이터입니다."),
  MEMBER_DUPLICATE(CONFLICT, "이미 가입된 계정이 존재합니다."),
  NICKNAME_DUPLICATE(CONFLICT, "해당 닉네임은 다른 사용자가 사용 중입니다."),

  START_DATE_AFTER_END_DATE(BAD_REQUEST, "endDate가 startDate보다 앞서 있습니다."),
  DAY_OF_WEEK_MAPPING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "나라별 요일 추출 에러입니다."),

  INVALID_EXPERIENCE_TYPE(BAD_REQUEST, "이색체험 타입은 ACTIVITY, CULTURE_AND_ARTS 만 가능합니다."),
  INVALID_EXPERIENCE_KEYWORD_TYPE(BAD_REQUEST, "잘못된 이색체험 키워드입니다."),

  INVALID_RESTAURANT_KEYWORD_TYPE(BAD_REQUEST, "잘못된 맛집 키워드입니다."),
  REVIEW_INVALID_CATEGORY(BAD_REQUEST, "해당 카테고리는 리뷰를 제공하지 않습니다."),
  REVIEW_INVALID_CATEGORY(BAD_REQUEST, "해당 카테고리는 리뷰를 제공하지 않습니다."),
  POST_NOT_FOUND(NOT_FOUND, "존재하는 post를 찾을 수 없습니다");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
