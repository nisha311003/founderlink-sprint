package com.founderlink.apiGateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Primary
    @Bean
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties config = new SwaggerUiConfigProperties();
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "auth-service",
                "/founderlink-docs/auth/v3/api-docs",
                "Auth Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "user-service",
                "/founderlink-docs/users/v3/api-docs",
                "User Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "startup-service",
                "/founderlink-docs/startups/v3/api-docs",
                "Startup Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "investment-service",
                "/founderlink-docs/investments/v3/api-docs",
                "Investment Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "team-service",
                "/founderlink-docs/teams/v3/api-docs",
                "Team Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "messaging-service",
                "/founderlink-docs/messages/v3/api-docs",
                "Messaging Service"
        ));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "notification-service",
                "/founderlink-docs/notifications/v3/api-docs",
                "Notification Service"
        ));

        config.setUrls(urls);
        return config;
    }
}
