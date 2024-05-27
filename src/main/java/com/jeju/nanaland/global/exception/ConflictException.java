package com.jeju.nanaland.global.exception;

public class ConflictException extends RuntimeException {

  public ConflictException() {
    super(ErrorCode.CONFLICT_DATA.getMessage());
  }

  public ConflictException(String errorMessage) {
    super(errorMessage);
  }
}
