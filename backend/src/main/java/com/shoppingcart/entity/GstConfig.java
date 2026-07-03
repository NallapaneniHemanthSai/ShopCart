package com.shoppingcart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Singleton configuration row (id is always 1) holding the store-wide GST rate.
 */
@Entity
@Table(name = "gst_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstConfig {

    @Id
    private Long id;

    @Column(name = "rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal ratePercent;
}
