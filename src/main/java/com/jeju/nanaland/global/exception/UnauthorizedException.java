package com.jeju.nanaland.global.exception;

public class UnauthorizedException extends RuntimeException {

  //로그인 유저가 아닐 경우, admin 개발되면 권한 구분
  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED_USER.getMessage());
  }

  public UnauthorizedException(String errorMessage) {
    super(errorMessage);
  }
}
