package com.jeju.nanaland.global.exception;

public class NotFoundException extends RuntimeException {

  public NotFoundException() {
    super(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
  }

  public NotFoundException(String errorMessage) {
    super(errorMessage);
  }
}
