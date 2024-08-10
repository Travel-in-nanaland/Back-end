package com.jeju.nanaland.global.exception;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
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

  // member profile
  UPDATE_MEMBER_PROFILE_SUCCESS(OK, "사용자 프로필 수정 성공"),
  GET_MEMBER_PROFILE_SUCCESS(OK, "사용자 프로필 조회 성공"),
  UPDATE_LANGUAGE_SUCCESS(OK, "언어 변경 성공"),

  // search
  SEARCH_SUCCESS(OK, "검색에 성공했습니다."),
  SEARCH_VOLUME_SUCCESS(OK, "검색량 UP 게시물 조회 성공"),

  // login
  JOIN_SUCCESS(OK, "회원 가입 성공"),
  LOGIN_SUCCESS(OK, "로그인에 성공했습니다."),
  REISSUE_TOKEN_SUCCESS(OK, "AccessToken, RefreshToken이 재발급되었습니다."),
  LOGOUT_SUCCESS(OK, "로그아웃 성공"),
  WITHDRAWAL_SUCCESS(OK, "회원 탈퇴 성공"),

  UPDATE_MEMBER_CONSENT_SUCCESS(OK, "이용약관 업데이트 성공"),

  // nana
  NANA_MAIN_SUCCESS(OK, "나나 메인 페이지 썸네일 조회 성공"),
  NANA_RECOMMEND_LIST_SUCCESS(OK, "나나 추천 게시물 조회 성공"),
  NANA_LIST_SUCCESS(OK, "나나 썸네일 리스트 조회 성공"),
  NANA_DETAIL_SUCCESS(OK, "나나 상세 페이지 조회 성공"),

  // nature
  NATURE_LIST_SUCCESS(OK, "7대 자연 썸네일 리스트 조회 성공"),
  NATURE_DETAIL_SUCCESS(OK, "7대 자연 상세 정보 조회 성공"),

  // favorite
  POST_LIKE_TOGGLE_SUCCESS(OK, "게시물 좋아요 토글 요청 성공"),
  GET_FAVORITE_LIST_SUCCESS(OK, "찜리스트 조회 성공"),

  // report
  POST_INFO_FIX_REPORT_SUCCESS(OK, "정보 수정 제안 요청 성공"),
  POST_REVIEW_REPORT_SUCCESS(OK, "리뷰 신고 요청 성공"),

  // festival
  FESTIVAL_LIST_SUCCESS(OK, "축제 썸네일 리스트 조회 성공"),
  FESTIVAL_DETAIL_SUCCESS(OK, "축제 상세정보 조회 성공"),

  // market
  MARKET_LIST_SUCCESS(OK, "전통시장 썸네일 리스트 조회 성공"),
  MARKET_DETAIL_SUCCESS(OK, "전통시장 상세정보 조회 성공"),

  // experience
  EXPERIENCE_LIST_SUCCESS(OK, "이색체험 썸네일 리스트 조회 성공"),
  EXPERIENCE_DETAIL_SUCCESS(OK, "이색체험 상세정보 조회 성공"),

  // restaurant
  RESTAURANT_LIST_SUCCESS(OK, "제주맛집 썸네일 리스트 조회 성공"),
  RESTAURANT_DETAIL_SUCCESS(OK, "제주맛집 상세정보 조회 성공"),

  // review
  REVIEW_LIST_SUCCESS(OK, "리뷰 리스트 조회 성공"),
  REVIEW_CREATED_SUCCESS(OK, "리뷰 생성 성공"),
  REVIEW_HEART_SUCCESS(OK, "리뷰 좋아요 토글 요청 성공"),
  MY_REVIEW_DETAIL_SUCCESS(OK, "직접 작성한 리뷰 조회 성공"),
  REVIEW_DELETE_SUCCESS(NO_CONTENT, "리뷰 삭제 성공"),
  REVIEW_UPDATE_SUCCESS(OK, "리뷰 수정 성공"),

  // notice
  NOTICE_LIST_SUCCESS(OK, "공지사항 리스트 조회 성공"),
  NOTICE_DETAIL_SUCCESS(OK, "공지사항 상세 조회 성공"),

  // notification
  NOTIFICATION_LIST_SUCCESS(OK, "알림 조회 성공"),
  SEND_NOTIFICATION_SUCCESS(CREATED, "알림 전송 성공");

  private final HttpStatus httpStatus;
  private final String message;

  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}
