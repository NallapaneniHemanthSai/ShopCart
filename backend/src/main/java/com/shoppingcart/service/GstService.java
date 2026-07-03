package com.shoppingcart.service;

import com.shoppingcart.dto.response.GstConfigResponse;

import java.math.BigDecimal;

public interface GstService {

    GstConfigResponse getConfig();

    GstConfigResponse updateRate(BigDecimal ratePercent);

    BigDecimal currentRate();
}
