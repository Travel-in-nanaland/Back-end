package com.jeju.nanaland.domain.restaurant.converter;

import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import org.springframework.core.convert.converter.Converter;

public class RestaurantTypeKeywordConverter implements Converter<String, RestaurantTypeKeyword> {

  @Override
  public RestaurantTypeKeyword convert(String source) {
    try {
      return RestaurantTypeKeyword.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorCode.INVALID_RESTAURANT_KEYWORD_TYPE.getMessage());
    }
  }
}
