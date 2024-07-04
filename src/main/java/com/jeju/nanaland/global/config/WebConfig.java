package com.jeju.nanaland.global.config;

import com.jeju.nanaland.domain.common.converter.CategoryRequestConverter;
import com.jeju.nanaland.global.auth.AuthMemberArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AuthMemberArgumentResolver authMemberArgumentResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authMemberArgumentResolver);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new CategoryRequestConverter());
  }
}
