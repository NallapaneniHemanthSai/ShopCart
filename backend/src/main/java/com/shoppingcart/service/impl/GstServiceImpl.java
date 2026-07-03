package com.shoppingcart.service.impl;

import com.shoppingcart.dto.response.GstConfigResponse;
import com.shoppingcart.entity.GstConfig;
import com.shoppingcart.repository.GstConfigRepository;
import com.shoppingcart.service.GstService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GstServiceImpl implements GstService {

    private static final Long SINGLETON_ID = 1L;
    private static final BigDecimal DEFAULT_RATE = BigDecimal.valueOf(18.00);

    private final GstConfigRepository gstConfigRepository;

    @Override
    public GstConfigResponse getConfig() {
        return new GstConfigResponse(loadOrCreate().getRatePercent());
    }

    @Override
    @Transactional
    public GstConfigResponse updateRate(BigDecimal ratePercent) {
        GstConfig config = loadOrCreate();
        config.setRatePercent(ratePercent);
        gstConfigRepository.save(config);
        return new GstConfigResponse(config.getRatePercent());
    }

    @Override
    public BigDecimal currentRate() {
        return loadOrCreate().getRatePercent();
    }

    private GstConfig loadOrCreate() {
        return gstConfigRepository.findById(SINGLETON_ID)
                .orElseGet(() -> gstConfigRepository.save(
                        GstConfig.builder().id(SINGLETON_ID).ratePercent(DEFAULT_RATE).build()));
    }
}
