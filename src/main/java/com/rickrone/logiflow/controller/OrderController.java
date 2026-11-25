package com.rickrone.logiflow.controller;

import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.model.dto.OrderCreateDTO;
import com.rickrone.logiflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  /**
   * 提交物资申领单（异步扣库存）
   * POST /api/orders
   */
  @PostMapping
  @PreAuthorize("hasAuthority('sys:order:add')")
  public Result<String> createOrder(@RequestBody @Valid OrderCreateDTO dto, Authentication authentication) {
    String orderNo = orderService.submitOrder(dto, authentication.getName());
    return Result.success(orderNo, "申领单已提交，正在排队扣减库存");
  }
}
