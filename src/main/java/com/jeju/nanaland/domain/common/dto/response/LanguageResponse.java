package com.jeju.nanaland.domain.common.dto.response;

import com.jeju.nanaland.domain.common.entity.Locale;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LanguageResponse {

  private Locale locale;
  private String dateFormat;
}
