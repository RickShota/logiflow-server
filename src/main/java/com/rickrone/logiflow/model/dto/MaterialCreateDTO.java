package com.rickrone.logiflow.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialCreateDTO {
  @NotBlank(message = "物资编码不能为空")
  private String materialCode;
  @NotBlank(message = "物资名称不能为空")
  private String materialName;

  private String category;
  @NotNull(message = "初始库存不能为空")
  @Min(value = 0, message = "初始库存不能小于 0")
  private Integer stock;
}
