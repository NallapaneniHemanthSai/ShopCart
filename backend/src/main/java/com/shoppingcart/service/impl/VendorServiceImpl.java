package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.VendorRequest;
import com.shoppingcart.dto.response.VendorResponse;
import com.shoppingcart.entity.Vendor;
import com.shoppingcart.exception.VendorNotFoundException;
import com.shoppingcart.mapper.VendorMapper;
import com.shoppingcart.repository.VendorRepository;
import com.shoppingcart.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    @Override
    public List<VendorResponse> listAll() {
        return vendorRepository.findAll().stream()
                .sorted(Comparator.comparing(Vendor::getName))
                .map(VendorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VendorResponse create(VendorRequest request) {
        Vendor vendor = Vendor.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .rating(request.rating())
                .verified(request.verified())
                .build();
        return VendorMapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    @Transactional
    public VendorResponse update(Long id, VendorRequest request) {
        Vendor vendor = requireById(id);
        vendor.setName(request.name());
        vendor.setEmail(request.email());
        vendor.setPhone(request.phone());
        vendor.setRating(request.rating());
        vendor.setVerified(request.verified());
        return VendorMapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    public Vendor requireById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new VendorNotFoundException(id));
    }
}
