package com.jeju.nanaland.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${springdoc.version}")
  private String version;

  @Bean
  public OpenAPI openAPI() {
    
    Info info = new Info()
        .title("NanaLand API 명세서")
        .description("nanaland in JEJU API 명세서입니다.")
        .version(version);

    return new OpenAPI()
        .components(new Components())
        .info(info);
  }
}
