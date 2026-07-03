package com.shoppingcart.config;

import com.shoppingcart.entity.*;
import com.shoppingcart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with an admin account, a starter product catalog, coupons,
 * and the default GST rate on first boot. Runs only when the relevant tables are empty
 * so it is safe to restart the app without duplicating data.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final GstConfigRepository gstConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed-email}")
    private String adminEmail;

    @Value("${app.admin.seed-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedProducts();
        seedCoupons();
        seedGst();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.save(User.builder()
                .name("Store Admin")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .phone("9999999999")
                .address("Head Office")
                .build());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                product("ELE-001", "Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", Category.ELECTRONICS, "699.00", 50),
                product("ELE-002", "Mechanical Keyboard", "RGB backlit mechanical keyboard", Category.ELECTRONICS, "2499.00", 30),
                product("ELE-003", "USB-C Hub", "7-in-1 USB-C hub with HDMI", Category.ELECTRONICS, "1299.00", 40),
                product("ELE-004", "Bluetooth Headphones", "Over-ear noise cancelling headphones", Category.ELECTRONICS, "3499.00", 25),
                product("ELE-005", "Power Bank 20000mAh", "Fast-charging power bank", Category.ELECTRONICS, "1799.00", 60),

                product("GRO-001", "Basmati Rice 5kg", "Premium long-grain basmati rice", Category.GROCERY, "549.00", 100),
                product("GRO-002", "Extra Virgin Olive Oil 1L", "Cold-pressed olive oil", Category.GROCERY, "699.00", 70),
                product("GRO-003", "Assorted Nuts 500g", "Almonds, cashews, pistachios mix", Category.GROCERY, "449.00", 80),
                product("GRO-004", "Green Tea 100 Bags", "Antioxidant-rich green tea", Category.GROCERY, "249.00", 90),

                product("FAS-001", "Cotton T-Shirt", "Unisex regular-fit cotton t-shirt", Category.FASHION, "499.00", 120),
                product("FAS-002", "Denim Jacket", "Classic blue denim jacket", Category.FASHION, "1999.00", 45),
                product("FAS-003", "Running Shoes", "Lightweight cushioned running shoes", Category.FASHION, "2799.00", 55),
                product("FAS-004", "Leather Wallet", "Genuine leather bifold wallet", Category.FASHION, "899.00", 65),

                product("HOM-001", "Air Fryer 4L", "Digital air fryer with 8 presets", Category.HOME_APPLIANCES, "4499.00", 20),
                product("HOM-002", "Robot Vacuum Cleaner", "Smart robotic vacuum with app control", Category.HOME_APPLIANCES, "12999.00", 12),
                product("HOM-003", "Electric Kettle 1.7L", "Stainless steel electric kettle", Category.HOME_APPLIANCES, "1099.00", 50),

                product("BOO-001", "Atomic Habits", "Bestselling self-improvement book", Category.BOOKS, "399.00", 75),
                product("BOO-002", "The Pragmatic Programmer", "Classic software engineering book", Category.BOOKS, "599.00", 40),

                product("TOY-001", "Building Blocks Set", "250-piece creative building blocks", Category.TOYS, "899.00", 35),
                product("TOY-002", "Remote Control Car", "High-speed RC car with rechargeable battery", Category.TOYS, "1599.00", 28),

                product("BEA-001", "Vitamin C Serum", "Brightening face serum 30ml", Category.BEAUTY, "649.00", 60),
                product("BEA-002", "Herbal Shampoo 400ml", "Sulphate-free herbal shampoo", Category.BEAUTY, "349.00", 85),

                product("SPO-001", "Yoga Mat", "Non-slip 6mm yoga mat", Category.SPORTS, "799.00", 50),
                product("SPO-002", "Adjustable Dumbbell Set", "5-25kg adjustable dumbbells (pair)", Category.SPORTS, "3999.00", 3)
        );

        productRepository.saveAll(products);
    }

    private Product product(String sku, String name, String description, Category category, String price, int stock) {
        return Product.builder()
                .sku(sku)
                .name(name)
                .description(description)
                .category(category)
                .price(new BigDecimal(price))
                .stock(stock)
                .active(true)
                .imageUrl(null)
                .build();
    }

    private void seedCoupons() {
        if (couponRepository.count() > 0) {
            return;
        }
        couponRepository.saveAll(List.of(
                Coupon.builder().code("SAVE10").discountType(DiscountType.PERCENTAGE)
                        .value(new BigDecimal("10")).minCartValue(new BigDecimal("500")).active(true).build(),
                Coupon.builder().code("FLAT100").discountType(DiscountType.FLAT)
                        .value(new BigDecimal("100")).minCartValue(new BigDecimal("999")).active(true).build(),
                Coupon.builder().code("WELCOME20").discountType(DiscountType.PERCENTAGE)
                        .value(new BigDecimal("20")).minCartValue(new BigDecimal("1500")).active(true).build()
        ));
    }

    private void seedGst() {
        if (gstConfigRepository.count() > 0) {
            return;
        }
        gstConfigRepository.save(GstConfig.builder().id(1L).ratePercent(new BigDecimal("18.00")).build());
    }
}
