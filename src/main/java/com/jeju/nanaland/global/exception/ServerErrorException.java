package com.jeju.nanaland.global.exception;

public class ServerErrorException extends RuntimeException {

  public ServerErrorException() {
    super("서버 에러입니다.");
  }

  public ServerErrorException(String errorMessage) {
    super(errorMessage);
  }
}
