package com.founderlink.apiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator founderLinkRouteConfig(RouteLocatorBuilder builder){

		return builder.routes()
				.route(p-> p
						.path("/founderlink/auth","/founderlink/auth/**")
						.filters(f-> f
								.rewritePath("/founderlink/auth/(?<segment>.*)",
										"/api/auth/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://AUTHSERVICE")
				)
				.route(p-> p
						.path("/founderlink/users","/founderlink/users/**")
						.filters(f-> f
								.rewritePath("/founderlink/users/?(?<segment>.*)",
										"/api/users/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://USERSERVICE")
				)
				.route(p -> p
						.path("/founderlink/startups", "/founderlink/startups/**")
						.filters(f -> f
								.rewritePath("/founderlink/startups/?(?<segment>.*)",
										"/api/startups/${segment}")
								.addResponseHeader("X-Response-Time",
										LocalDateTime.now().toString()))
						.uri("lb://STARTUPSERVICE"))
				.route(p -> p
						.path("/founderlink/investments",
								"/founderlink/investments/**")
						.filters(f -> f
								.rewritePath(
										"/founderlink/investments/?(?<segment>.*)",
										"/api/investments/${segment}")
								.addResponseHeader("X-Response-Time",
										LocalDateTime.now().toString()))
						.uri("lb://INVESTMENTSERVICE"))
				.route(p -> p
						.path("/founderlink/teams",
								"/founderlink/teams/**")
						.filters(f -> f
								.rewritePath(
										"/founderlink/teams/?(?<segment>.*)",
										"/api/teams/${segment}")
								.addResponseHeader("X-Response-Time",
										LocalDateTime.now().toString()))
						.uri("lb://TEAMSERVICE"))
				.route(p -> p
						.path("/founderlink/messages",
								"/founderlink/messages/**")
						.filters(f -> f
								.rewritePath(
										"/founderlink/messages/?(?<segment>.*)",
										"/api/messages/${segment}")
								.addResponseHeader("X-Response-Time",
										LocalDateTime.now().toString()))
						.uri("lb://MESSAGINGSERVICE"))
				.route(p -> p
						.path("/founderlink/notifications",
								"/founderlink/notifications/**")
						.filters(f -> f
								.rewritePath(
										"/founderlink/notifications/?(?<segment>.*)",
										"/api/notifications/${segment}")
								.addResponseHeader("X-Response-Time",
										LocalDateTime.now().toString()))
						.uri("lb://NOTIFICATIONSERVICE"))
				// ✅ Swagger doc routes
				.route(p -> p
						.path("/founderlink-docs/auth/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/auth/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://AUTHSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/users/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/users/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://USERSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/startups/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/startups/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://STARTUPSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/investments/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/investments/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://INVESTMENTSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/teams/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/teams/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://TEAMSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/messages/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/messages/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://MESSAGINGSERVICE"))

				.route(p -> p
						.path("/founderlink-docs/notifications/**")
						.filters(f -> f
								.rewritePath("/founderlink-docs/notifications/?(?<segment>.*)",
										"/${segment}"))
						.uri("lb://NOTIFICATIONSERVICE"))

				.build();
	}

}
