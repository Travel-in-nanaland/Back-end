package com.jeju.nanaland.domain.common.converter;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import org.springframework.core.convert.converter.Converter;

public class CategoryRequestConverter implements Converter<String, Category> {

  @Override
  public Category convert(String source) {
    try {
      return Category.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
  }
}
