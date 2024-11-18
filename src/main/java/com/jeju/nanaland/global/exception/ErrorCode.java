package com.jeju.nanaland.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // BAD_REQUEST
  BAD_REQUEST_EXCEPTION(BAD_REQUEST, "잘못된 요청입니다."),
  REQUEST_VALIDATION_EXCEPTION(BAD_REQUEST, "입력 형태가 잘못된 요청입니다."),
  MEMBER_CONSENT_BAD_REQUEST(BAD_REQUEST, "TERMS_OF_USE는 필수로 동의해야 합니다."),
  IMAGE_BAD_REQUEST(BAD_REQUEST, "이미지는 최대 5장까지 가능합니다."),
  REVIEW_IMAGE_BAD_REQUEST(BAD_REQUEST, "리뷰 이미지는 최대 5장까지 가능합니다."),
  START_DATE_AFTER_END_DATE(BAD_REQUEST, "endDate가 startDate보다 앞서 있습니다."),
  INVALID_EXPERIENCE_TYPE(BAD_REQUEST, "이색체험 타입은 ACTIVITY, CULTURE_AND_ARTS 만 가능합니다."),
  INVALID_RESTAURANT_KEYWORD_TYPE(BAD_REQUEST, "잘못된 맛집 키워드입니다."),
  REVIEW_INVALID_CATEGORY(BAD_REQUEST, "해당 카테고리는 리뷰를 제공하지 않습니다."),
  REVIEW_KEYWORD_DUPLICATION(BAD_REQUEST, "리뷰 카테고리가 중복 되었습니다."),
  NOT_MY_REVIEW(BAD_REQUEST, "자신의 리뷰만 삭제할 수 있습니다."),
  REVIEW_IMAGE_IMAGE_INFO_NOT_MATCH(BAD_REQUEST, "리뷰 수정 시 이미지 리스트와 수정 이미지 정보 리스트 길이가 일치하지 않습니다."),
  EDIT_REVIEW_IMAGE_INFO_BAD_REQUEST(BAD_REQUEST, "리뷰 수정 시 기존 이미지에 대한 정보가 잘못 되었습니다."),
  SELF_REPORT_NOT_ALLOWED(BAD_REQUEST, "본인을 신고하는 요청은 유효하지 않습니다."),
  ALREADY_REPORTED(BAD_REQUEST, "이미 신고되었습니다."),
  NO_NOTIFICATION_CONSENT(BAD_REQUEST, "알림 동의를 하지 않은 유저입니다."),
  INVALID_FILE_SIZE(BAD_REQUEST, "파일 크기가 유효하지 않습니다."),
  INVALID_FILE_EXTENSION_TYPE(BAD_REQUEST, "해당 카테고리에서 지원하지 않는 파일 형식입니다."),
  NO_FILE_EXTENSION(BAD_REQUEST, "파일 확장자가 없습니다."),

  //INTERNAL_SERVER_ERROR
  SERVER_ERROR(INTERNAL_SERVER_ERROR, "서버측 에러입니다."),
  EXTRACT_NAME_ERROR(INTERNAL_SERVER_ERROR, "이미지 파일 이름 추출 에러"),
  MAIL_FAIL_ERROR(INTERNAL_SERVER_ERROR, "메일 전송 실패"),
  FILE_FAIL_ERROR(INTERNAL_SERVER_ERROR, "파일 변환 중 오류가 발생했습니다."),
  FILE_UPLOAD_FAIL(INTERNAL_SERVER_ERROR, "파일 업로드 실패"),

  //UNAUTHORIZED
  UNAUTHORIZED_USER(UNAUTHORIZED, "access token이 존재하지 않습니다."),
  EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
  ACCESS_DENIED(UNAUTHORIZED, "접근 권한이 없습니다."),
  INVALID_TOKEN(UNAUTHORIZED, "토큰이 유효하지 않습니다."),

  // UNSUPPORTED_MEDIA_TYPE
  UNSUPPORTED_FILE_FORMAT(UNSUPPORTED_MEDIA_TYPE, "적절하지 않은 확장자의 파일입니다."),

  // NOT_FOUND
  NOT_FOUND_EXCEPTION(NOT_FOUND, "존재하는 데이터를 찾을 수 없습니다."),
  MEMBER_NOT_FOUND(NOT_FOUND, "존재하는 회원을 찾을 수 없습니다."),
  NANA_NOT_FOUND(NOT_FOUND, "존재하지 않는 Nana 입니다."),
  NANA_TITLE_NOT_FOUND(NOT_FOUND, "존재하지 않는 Nana Title 입니다."),
  MEMBER_CONSENT_NOT_FOUND(NOT_FOUND, "존재하는 이용약관 동의 여부를 찾을 수 없습니다"),
  MEMBER_WITHDRAWAL_NOT_FOUND(NOT_FOUND, "존재하는 회원의 탈퇴 상태를 찾을 수 없습니다"),
  CATEGORY_NOT_FOUND(NOT_FOUND, "존재하는 카테고리를 찾을 수 없습니다."),
  REVIEW_NOT_FOUND(NOT_FOUND, "존재하는 리뷰를 찾을 수 없습니다."),
  MEMBER_REVIEW_NOT_FOUND(NOT_FOUND, "유저가 작성한 리뷰를 찾을 수 없습니다."),
  NOTICE_NOT_FOUND(NOT_FOUND, "존재하는 공지사항을 찾을 수 없습니다."),
  KEYWORD_NOT_FOUND(NOT_FOUND, "존재하지 않는 키워드 입니다."),
  INFO_TYPE_NOT_FOUND(NOT_FOUND, "존재하지 않는 InfoType 입니다."),
  LANGUAGE_NOT_FOUND(NOT_FOUND, "지원하지 않는 언어입니다."),
  FILE_S3_NOT_FOUNE(NOT_FOUND, "파일을 S3에서 찾을 수 없습니다."),

  // CONFLICT
  CONFLICT_DATA(CONFLICT, "이미 존재하는 데이터입니다."),
  MEMBER_DUPLICATE(CONFLICT, "이미 가입된 계정이 존재합니다."),
  NICKNAME_DUPLICATE(CONFLICT, "해당 닉네임은 다른 사용자가 사용 중입니다."),

  // FORBIDDEN
  NANA_INFO_FIX_FORBIDDEN(FORBIDDEN, "나나스픽 게시물은 정보 수정 요청이 불가능합니다."),
  FORBIDDEN_EXCEPTION(FORBIDDEN, "접근 권한이 없습니다."),
  REVIEW_SELF_LIKE_FORBIDDEN(FORBIDDEN, "본인의 리뷰는 좋아요를 누를 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
