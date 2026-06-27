package com.ecc.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Entry point for the ECC Job Service Spring Boot application.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@OpenAPIDefinition(
    info = @Info(
        title = "${api.title}",
        version = "${api.version}",
        description = "${api.description}",
        license = @License( name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html" ),
        contact = @Contact( name = "Damian", email = "damiankk15@interia.pl" ) ),
    security = @SecurityRequirement( name = "Authorization" ),
    servers = { @Server( url = "/", description = "Via Gateway" ) } )
@SecurityScheme(
    name = "Authorization",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER )
@EnableSpringDataWebSupport( pageSerializationMode = PageSerializationMode.DIRECT )
@SpringBootApplication
public class Application
{
    /**
     * Starts the Spring Boot application.
     * 
     * @param aArgs command-line arguments passed to the application
     */
    public static void main( String[] aArgs )
    {
        SpringApplication.run( Application.class, aArgs );
    }
}
