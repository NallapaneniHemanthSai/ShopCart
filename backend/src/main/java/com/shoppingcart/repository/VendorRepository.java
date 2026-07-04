package com.shoppingcart.repository;

import com.shoppingcart.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
