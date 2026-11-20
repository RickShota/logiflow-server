package com.rickrone.logiflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rickrone.logiflow.model.dto.MaterialCreateDTO;
import com.rickrone.logiflow.model.dto.MaterialUpdateDTO;
import com.rickrone.logiflow.model.vo.MaterialVO;

public interface MaterialService {
  Long createMaterial(MaterialCreateDTO dto);

  void updateMaterial(MaterialUpdateDTO dto);

  void deleteMaterial(Long id);

  MaterialVO getMaterialById(Long id);

  Page<MaterialVO> getMaterialPage(long pageNum, long pageSize, String keyword, String category);
}
