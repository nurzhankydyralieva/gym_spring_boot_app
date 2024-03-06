package com.epam.xstack;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(info = @Info(title = "GYM API", version = "1.0", description = "This is a sample API for Spring Boot application for EPAM's xStack program."))
@SecurityScheme(name = "gym_spring_boot_application", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SpringBootApplication
public class GymSpringBootRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSpringBootRestApplication.class, args);
    }
}
