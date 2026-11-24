package com.rickrone.logiflow.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

  @NotNull(message = "物资ID不能为空")
  private Long materialId;

  @NotNull(message = "申领数量不能为空")
  @Min(value = 1, message = "申领数量至少为 1")
  private Integer quantity;

  private String remark;
}
