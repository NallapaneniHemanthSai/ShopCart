package com.shoppingcart.service.impl;

import com.shoppingcart.entity.Order;
import com.shoppingcart.entity.OrderItem;
import com.shoppingcart.service.FileExportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class FileExportServiceImpl implements FileExportService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final String LINE = "-".repeat(60);

    @Override
    public byte[] toTxt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOPPING CART LINE ITEM MANAGER\n");
        sb.append("INVOICE\n");
        sb.append(LINE).append("\n");
        sb.append("Invoice No : ").append(order.getInvoiceNumber()).append("\n");
        sb.append("Date       : ").append(TIMESTAMP_FMT.format(order.getCreatedAt())).append("\n");
        sb.append("Customer   : ").append(order.getShippingName()).append("\n");
        sb.append("Phone      : ").append(order.getShippingPhone()).append("\n");
        sb.append("Address    : ").append(order.getShippingAddress()).append("\n");
        sb.append("Payment    : ").append(order.getPaymentMethod()).append("\n");
        sb.append(LINE).append("\n");
        sb.append(String.format("%-12s %-24s %8s %6s %10s%n", "SKU", "PRODUCT", "PRICE", "QTY", "TOTAL"));
        sb.append(LINE).append("\n");
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("%-12s %-24s %8.2f %6d %10.2f%n",
                    item.getProductSku(), truncate(item.getProductName(), 24),
                    item.getUnitPrice(), item.getQuantity(), item.getLineTotal()));
        }
        sb.append(LINE).append("\n");
        sb.append(String.format("%-42s %17.2f%n", "Subtotal", order.getSubtotal()));
        if (order.getCouponCode() != null) {
            sb.append(String.format("%-42s %17s%n", "Coupon (" + order.getCouponCode() + ")",
                    "-" + String.format("%.2f", order.getDiscountAmount())));
        }
        sb.append(String.format("%-42s %17.2f%n", "GST (" + order.getGstRate() + "%)", order.getGstAmount()));
        sb.append(String.format("%-42s %17.2f%n", "Delivery Charge", order.getDeliveryCharge()));
        sb.append(LINE).append("\n");
        sb.append(String.format("%-42s %17.2f%n", "TOTAL", order.getTotalAmount()));
        sb.append(LINE).append("\n");
        sb.append("Thank you for shopping with us!\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toCsv(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice Number,").append(order.getInvoiceNumber()).append("\n");
        sb.append("Date,").append(TIMESTAMP_FMT.format(order.getCreatedAt())).append("\n");
        sb.append("Customer,").append(csvEscape(order.getShippingName())).append("\n");
        sb.append("Payment Method,").append(order.getPaymentMethod()).append("\n");
        sb.append("\n");
        sb.append("SKU,Product,Category,Unit Price,Quantity,Line Total\n");
        for (OrderItem item : order.getItems()) {
            sb.append(csvEscape(item.getProductSku())).append(",")
                    .append(csvEscape(item.getProductName())).append(",")
                    .append(item.getCategory()).append(",")
                    .append(item.getUnitPrice()).append(",")
                    .append(item.getQuantity()).append(",")
                    .append(item.getLineTotal()).append("\n");
        }
        sb.append("\n");
        sb.append("Subtotal,").append(order.getSubtotal()).append("\n");
        sb.append("Coupon Code,").append(order.getCouponCode() == null ? "" : order.getCouponCode()).append("\n");
        sb.append("Discount,").append(order.getDiscountAmount()).append("\n");
        sb.append("GST Rate %,").append(order.getGstRate()).append("\n");
        sb.append("GST Amount,").append(order.getGstAmount()).append("\n");
        sb.append("Delivery Charge,").append(order.getDeliveryCharge()).append("\n");
        sb.append("Total Amount,").append(order.getTotalAmount()).append("\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String truncate(String value, int maxLen) {
        return value.length() <= maxLen ? value : value.substring(0, maxLen - 1) + "…";
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
