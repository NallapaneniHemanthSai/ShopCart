package com.shoppingcart.util;

import com.shoppingcart.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates invoice numbers of the form INV-YYYYMMDD-000123. The running
 * sequence is seeded from the current order count at boot so numbers stay
 * unique (within a day) across application restarts, and AtomicLong keeps
 * concurrent checkouts race-free without needing a DB sequence.
 */
@Component
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private AtomicLong sequence;

    @PostConstruct
    void init() {
        sequence = new AtomicLong(orderRepository.count());
    }

    public String next() {
        long seq = sequence.incrementAndGet();
        String datePart = LocalDate.now().format(DATE_FMT);
        return "INV-" + datePart + "-" + String.format("%06d", seq);
    }
}
