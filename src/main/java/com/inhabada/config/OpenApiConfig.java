package com.inhabada.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI inhaBadaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("인하바다 (Inha-Bada) API")
                        .version("v0.0.1")
                        .description("인하대학교 캠퍼스 나눔 플랫폼 백엔드 API. "
                                + "로그인(/api/auth/login)으로 발급받은 세션 토큰을 우측 상단 Authorize에 입력해 인증 API를 테스트할 수 있습니다."))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SCHEME_NAME,
                        new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("로그인 시 발급되는 UUID 세션 토큰")));
    }
}
