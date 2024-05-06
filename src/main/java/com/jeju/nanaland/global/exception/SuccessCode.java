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

  // member consent
  CONSENT_UPDATE_SUCCESS(OK, "이용 약관 동의 여부 업데이트 성공"),

  // search
  SEARCH_SUCCESS(OK, "검색에 성공했습니다."),
  SEARCH_VOLUME_SUCCESS(OK, "검색량 UP 게시물 조회 성공"),

  // login
  LOGIN_SUCCESS(OK, "로그인에 성공했습니다."),
  REISSUE_TOKEN_SUCCESS(OK, "AccessToken, RefreshToken이 재발급되었습니다."),
  LOGOUT_SUCCESS(OK, "로그아웃 성공"),

  // nana
  NANA_MAIN_SUCCESS(OK, "나나 메인 페이지 썸네일 조회 성공"),
  NANA_LIST_SUCCESS(OK, "나나 썸네일 리스트 조회 성공"),
  NANA_DETAIL_SUCCESS(OK, "나나 상세 페이지 조회 성공"),

  // nature
  NATURE_LIST_SUCCESS(OK, "7대 자연 썸네일 리스트 조회 성공"),
  NATURE_DETAIL_SUCCESS(OK, "7대 자연 상세 정보 조회 성공"),

  // favorite
  POST_LIKE_TOGGLE_SUCCESS(OK, "게시물 좋아요 토글 요청 성공"),

  // festival
  FESTIVAL_LIST_SUCCESS(OK, "축제 썸네일 리스트 조회 성공"),
  FESTIVAL_DETAIL_SUCCESS(OK, "축제 상세정보 조회 성공"),

  // market
  MARKET_LIST_SUCCESS(OK, "전통시장 썸네일 리스트 조회 성공"),
  MARKET_DETAIL_SUCCESS(OK, "전통시장 상세정보 조회 성공"),

  // favorite
  GET_FAVORITE_LIST_SUCCESS(OK, "찜리스트 조회 성공");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
