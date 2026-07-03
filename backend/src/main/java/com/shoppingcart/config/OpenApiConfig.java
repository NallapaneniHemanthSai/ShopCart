package com.shoppingcart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Shopping Cart Line Item Manager API",
                version = "1.0.0",
                description = "Full-stack shopping cart REST API: catalog, cart, wishlist, coupons, "
                        + "checkout with GST/discount/delivery pricing, invoice export, and an admin panel "
                        + "with sales analytics. Use the Authorize button with a JWT from /api/auth/login "
                        + "or /api/auth/register to call protected endpoints.",
                contact = @Contact(name = "Shopping Cart Manager")
        ),
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
