package com.jeju.nanaland.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(servers = {@Server(url = "https://nanaland.site")})
public class SwaggerConfig {

  @Value("${springdoc.version}")
  private String version;

  @Bean
  public OpenAPI openAPI() {

    SecurityScheme securityScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP).scheme("Bearer").bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER).name("Authorization");
    SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");

    Components components = new Components().addSecuritySchemes("JWT", securityScheme);

    Info info = new Info()
        .title("NanaLand API 명세서")
        .description("nanaland in JEJU API 명세서입니다.")
        .version(version);

    return new OpenAPI()
        .info(info)
        .components(components)
        .addSecurityItem(securityRequirement);
  }
}
