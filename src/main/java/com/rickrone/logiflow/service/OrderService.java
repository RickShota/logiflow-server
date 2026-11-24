package com.rickrone.logiflow.service;

import com.rickrone.logiflow.model.dto.OrderCreateDTO;

public interface OrderService {
  String submitOrder(OrderCreateDTO dto, String username);
}
