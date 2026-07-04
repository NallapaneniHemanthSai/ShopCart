package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.VendorResponse;
import com.shoppingcart.entity.Vendor;

public final class VendorMapper {

    private VendorMapper() {
    }

    public static VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getEmail(),
                vendor.getPhone(),
                vendor.getRating(),
                vendor.isVerified()
        );
    }
}
