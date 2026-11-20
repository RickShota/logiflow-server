package com.rickrone.logiflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialVO {
  private Long id;
  private String materialCode;
  private String materialName;
  private String category;
  private Integer stock;
  private LocalDateTime createTime;
}
