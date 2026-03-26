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

				.build();
	}

}
