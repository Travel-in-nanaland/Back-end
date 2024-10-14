package com.jeju.nanaland.global.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException() {
    super(ErrorCode.BAD_REQUEST_EXCEPTION.getMessage());
  }

  public BadRequestException(ErrorCode errorCode) {
    super(errorCode.getMessage());
  }

  public BadRequestException(String errorMessage) {
    super(errorMessage);
  }
}
