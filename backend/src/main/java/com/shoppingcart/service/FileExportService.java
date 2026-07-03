package com.shoppingcart.service;

import com.shoppingcart.entity.Order;

public interface FileExportService {

    byte[] toTxt(Order order);

    byte[] toCsv(Order order);
}
