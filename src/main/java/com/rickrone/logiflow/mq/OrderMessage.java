package com.rickrone.logiflow.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {
  private String orderNo;
  private Long userId;
  private Long materialId;
  private Integer quantity;
}
