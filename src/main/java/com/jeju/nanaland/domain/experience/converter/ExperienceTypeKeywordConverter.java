package com.jeju.nanaland.domain.experience.converter;

import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import org.springframework.core.convert.converter.Converter;

public class ExperienceTypeKeywordConverter implements Converter<String, ExperienceTypeKeyword> {

  @Override
  public ExperienceTypeKeyword convert(String source) {
    try {
      return ExperienceTypeKeyword.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorCode.INVALID_EXPERIENCE_TYPE.getMessage());
    }
  }
}
