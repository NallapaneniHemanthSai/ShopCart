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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the database with an admin account, a marketplace of vendors, a starter
 * product catalog (including the same product listed by multiple vendors), coupons,
 * and the default GST rate on first boot. Runs only when the relevant tables are empty
 * so it is safe to restart the app without duplicating data.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final CouponRepository couponRepository;
    private final GstConfigRepository gstConfigRepository;
    private final PasswordEncoder passwordEncoder;

    private Map<String, Vendor> vendors;

    @Value("${app.admin.seed-email}")
    private String adminEmail;

    @Value("${app.admin.seed-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedVendors();
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
                .loyaltyPoints(0)
                .build());
    }

    private void seedVendors() {
        if (vendorRepository.count() > 0) {
            vendors = new HashMap<>();
            vendorRepository.findAll().forEach(v -> vendors.put(v.getName(), v));
            return;
        }

        List<Vendor> toSave = List.of(
                vendor("TechWorld Store", "contact@techworld.example", "9810000001", "4.60", true),
                vendor("Urban Basket", "hello@urbanbasket.example", "9810000002", "4.20", true),
                vendor("Prime Traders", "sales@primetraders.example", "9810000003", "4.80", true),
                vendor("Value Mart", "support@valuemart.example", "9810000004", "3.90", false),
                vendor("QuickShip Hub", "care@quickshiphub.example", "9810000005", "4.40", true)
        );
        List<Vendor> saved = vendorRepository.saveAll(toSave);

        vendors = new HashMap<>();
        saved.forEach(v -> vendors.put(v.getName(), v));
    }

    private Vendor vendor(String name, String email, String phone, String rating, boolean verified) {
        return Vendor.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .rating(new BigDecimal(rating))
                .verified(verified)
                .build();
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        Vendor techWorld = vendors.get("TechWorld Store");
        Vendor urbanBasket = vendors.get("Urban Basket");
        Vendor primeTraders = vendors.get("Prime Traders");
        Vendor valueMart = vendors.get("Value Mart");
        Vendor quickShip = vendors.get("QuickShip Hub");

        List<Product> products = new ArrayList<>(List.of(
                product("ELE-001", "Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", Category.ELECTRONICS, "699.00", 50, techWorld),
                product("ELE-002", "Mechanical Keyboard", "RGB backlit mechanical keyboard", Category.ELECTRONICS, "2499.00", 30, techWorld),
                product("ELE-003", "USB-C Hub", "7-in-1 USB-C hub with HDMI", Category.ELECTRONICS, "1299.00", 40, techWorld),
                product("ELE-004", "Bluetooth Headphones", "Over-ear noise cancelling headphones", Category.ELECTRONICS, "3499.00", 25, techWorld),
                product("ELE-005", "Power Bank 20000mAh", "Fast-charging power bank", Category.ELECTRONICS, "1799.00", 60, techWorld),

                product("GRO-001", "Basmati Rice 5kg", "Premium long-grain basmati rice", Category.GROCERY, "549.00", 100, urbanBasket),
                product("GRO-002", "Extra Virgin Olive Oil 1L", "Cold-pressed olive oil", Category.GROCERY, "699.00", 70, urbanBasket),
                product("GRO-003", "Assorted Nuts 500g", "Almonds, cashews, pistachios mix", Category.GROCERY, "449.00", 80, urbanBasket),
                product("GRO-004", "Green Tea 100 Bags", "Antioxidant-rich green tea", Category.GROCERY, "249.00", 90, urbanBasket),

                product("FAS-001", "Cotton T-Shirt", "Unisex regular-fit cotton t-shirt", Category.FASHION, "499.00", 120, primeTraders),
                product("FAS-002", "Denim Jacket", "Classic blue denim jacket", Category.FASHION, "1999.00", 45, primeTraders),
                product("FAS-003", "Running Shoes", "Lightweight cushioned running shoes", Category.FASHION, "2799.00", 55, primeTraders),
                product("FAS-004", "Leather Wallet", "Genuine leather bifold wallet", Category.FASHION, "899.00", 65, primeTraders),

                product("HOM-001", "Air Fryer 4L", "Digital air fryer with 8 presets", Category.HOME_APPLIANCES, "4499.00", 20, techWorld),
                product("HOM-002", "Robot Vacuum Cleaner", "Smart robotic vacuum with app control", Category.HOME_APPLIANCES, "12999.00", 12, techWorld),
                product("HOM-003", "Electric Kettle 1.7L", "Stainless steel electric kettle", Category.HOME_APPLIANCES, "1099.00", 50, techWorld),

                product("BOO-001", "Atomic Habits", "Bestselling self-improvement book", Category.BOOKS, "399.00", 75, quickShip),
                product("BOO-002", "The Pragmatic Programmer", "Classic software engineering book", Category.BOOKS, "599.00", 40, quickShip),

                product("TOY-001", "Building Blocks Set", "250-piece creative building blocks", Category.TOYS, "899.00", 35, quickShip),
                product("TOY-002", "Remote Control Car", "High-speed RC car with rechargeable battery", Category.TOYS, "1599.00", 28, quickShip),

                product("BEA-001", "Vitamin C Serum", "Brightening face serum 30ml", Category.BEAUTY, "649.00", 60, urbanBasket),
                product("BEA-002", "Herbal Shampoo 400ml", "Sulphate-free herbal shampoo", Category.BEAUTY, "349.00", 85, urbanBasket),

                product("SPO-001", "Yoga Mat", "Non-slip 6mm yoga mat", Category.SPORTS, "799.00", 50, valueMart),
                product("SPO-002", "Adjustable Dumbbell Set", "5-25kg adjustable dumbbells (pair)", Category.SPORTS, "3999.00", 3, valueMart)
        ));

        // Marketplace duplicates: the same conceptual product listed by a different
        // vendor at a different price/stock, under its own SKU — this is what lets
        // the catalog show "3 sellers, from ₹X" instead of one fixed listing per item.
        products.addAll(List.of(
                product("ELE-001-B", "Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", Category.ELECTRONICS, "649.00", 30, valueMart),
                product("ELE-001-C", "Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", Category.ELECTRONICS, "749.00", 20, primeTraders),
                product("ELE-002-B", "Mechanical Keyboard", "RGB backlit mechanical keyboard", Category.ELECTRONICS, "2399.00", 15, valueMart),
                product("GRO-001-B", "Basmati Rice 5kg", "Premium long-grain basmati rice", Category.GROCERY, "519.00", 60, valueMart),
                product("FAS-001-B", "Cotton T-Shirt", "Unisex regular-fit cotton t-shirt", Category.FASHION, "459.00", 80, valueMart),
                product("BOO-001-B", "Atomic Habits", "Bestselling self-improvement book", Category.BOOKS, "429.00", 30, primeTraders),
                product("SPO-001-B", "Yoga Mat", "Non-slip 6mm yoga mat", Category.SPORTS, "749.00", 40, urbanBasket)
        ));

        productRepository.saveAll(products);
    }

    private Product product(String sku, String name, String description, Category category, String price, int stock, Vendor vendor) {
        return Product.builder()
                .sku(sku)
                .name(name)
                .description(description)
                .category(category)
                .vendor(vendor)
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
