package com.demoApp.menu_module.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI menuServiceOpenAPI() {
        Server devServer = new Server()
            .url("http://localhost:8083")
            .description("Development server");

        Contact contact = new Contact()
            .name("DemoApp Team")
            .email("support@demoapp.com");

        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
            .title("Menu Service API")
            .version("1.0.0")
            .description("API documentation for the Menu Service of DemoApp")
            .contact(contact)
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer));
    }
}