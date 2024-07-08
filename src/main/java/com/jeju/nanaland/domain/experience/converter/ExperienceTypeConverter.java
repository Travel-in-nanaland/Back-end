package com.jeju.nanaland.domain.experience.converter;

import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import org.springframework.core.convert.converter.Converter;

public class ExperienceTypeConverter implements Converter<String, ExperienceType> {

  @Override
  public ExperienceType convert(String source) {
    try {
      return ExperienceType.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorCode.INVALID_EXPERIENCE_TYPE.getMessage());
    }
  }
}
