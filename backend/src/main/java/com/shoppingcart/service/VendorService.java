package com.shoppingcart.service;

import com.shoppingcart.dto.request.VendorRequest;
import com.shoppingcart.dto.response.VendorResponse;
import com.shoppingcart.entity.Vendor;

import java.util.List;

public interface VendorService {

    List<VendorResponse> listAll();

    VendorResponse create(VendorRequest request);

    VendorResponse update(Long id, VendorRequest request);

    Vendor requireById(Long id);
}
