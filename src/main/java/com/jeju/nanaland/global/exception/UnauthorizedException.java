package com.jeju.nanaland.global.exception;

public class UnauthorizedException extends RuntimeException{

//로그인 유저가 아닐 경우, admin 개발되면 권한 구분
  public UnauthorizedException() {
    super("사용 권한이 없습니다.");
  }
}
