package com.jeju.nanaland.global.exception;

public class UnsupportedFileFormatException extends RuntimeException {

  public UnsupportedFileFormatException() {
    super(ErrorCode.UNSUPPORTED_FILE_FORMAT.getMessage());
  }

  public UnsupportedFileFormatException(String message) {
    super(message);
  }

}
