package com.jeju.nanaland.domain.common.converter;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import org.springframework.core.convert.converter.Converter;

public class LanguageRequestConverter implements Converter<String, Language> {

  @Override
  public Language convert(String source) {
    try {
      return Language.valueOf(source);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorCode.INVALID_LANGUAGE.getMessage());
    }
  }
}
