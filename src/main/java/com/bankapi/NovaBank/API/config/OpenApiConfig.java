package com.bankapi.NovaBank.API.config;

import com.bankapi.NovaBank.API.dto.response.ErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI novaBankOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("NovaBank API")
                        .version("1.0.0")
                        .description(
                                "REST API for NovaBank account and transaction management."
                        ))

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    @Bean
    public GlobalOpenApiCustomizer globalErrorResponses() {

        return openAPI -> {

            Schema<ErrorResponse> errorSchema =
                    new Schema<ErrorResponse>()
                            .$ref("#/components/schemas/ErrorResponse");

            Content errorContent = new Content()
                    .addMediaType(
                            "application/json",
                            new MediaType()
                                    .schema(errorSchema)
                    );

            openAPI.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {

                        // 400 - Bad Request
                        operation.getResponses().addApiResponse(
                                "400",
                                new ApiResponse()
                                        .description("Bad Request")
                                        .content(errorContent)
                        );

                        // 401 - Unauthorized
                        operation.getResponses().addApiResponse(
                                "401",
                                new ApiResponse()
                                        .description("Unauthorized")
                                        .content(errorContent)
                        );

                        // 404 - Not Found
                        operation.getResponses().addApiResponse(
                                "404",
                                new ApiResponse()
                                        .description("Resource not found")
                                        .content(errorContent)
                        );

                        // 409 - Conflict
                        operation.getResponses().addApiResponse(
                                "409",
                                new ApiResponse()
                                        .description("Resource already exists")
                                        .content(errorContent)
                        );

                        // 415 - Unsupported Media Type
                        operation.getResponses().addApiResponse(
                                "415",
                                new ApiResponse()
                                        .description("Unsupported media type")
                                        .content(errorContent)
                        );

                        // 500 - Internal Server Error
                        operation.getResponses().addApiResponse(
                                "500",
                                new ApiResponse()
                                        .description("Internal server error")
                                        .content(errorContent)
                        );
                    })
            );
        };
    }
}