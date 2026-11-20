package com.rickrone.logiflow.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialUpdateDTO {
  @NotNull(message = "物资ID不能为空")
  private Long id;

  @NotBlank(message = "物资名称不能为空")
  private String materialName;

  private String category;

  @NotNull(message = "库存数量不能为空")
  @Min(value = 0, message = "库存数量不能小于 0")
  private Integer stock;
}
