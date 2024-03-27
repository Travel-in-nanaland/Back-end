package com.jeju.nanaland.global.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException() {
    super("올바른 요청이 아닙니다.");
  }

  public BadRequestException(String errorMessage) {
    super(errorMessage);
  }
}
