package com.jeju.nanaland.global.exception;

public class ForbiddenException extends RuntimeException {

  public ForbiddenException() {
    super(ErrorCode.FORBIDDEN_EXCEPTION.getMessage());
  }

  public ForbiddenException(String errorMessage) {
    super(errorMessage);
  }
}
