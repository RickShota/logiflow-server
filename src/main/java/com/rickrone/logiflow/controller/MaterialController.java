package com.rickrone.logiflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.model.dto.MaterialCreateDTO;
import com.rickrone.logiflow.model.dto.MaterialUpdateDTO;
import com.rickrone.logiflow.model.vo.MaterialVO;
import com.rickrone.logiflow.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {
  private final MaterialService materialService;

  /**
   * 新增物资
   * POST /api/materials
   */
  @PostMapping("/add")
  public Result<Long> create(@RequestBody @Validated MaterialCreateDTO dto) {
    Long id = materialService.createMaterial(dto);
    return Result.success(id, "新增物资成功");
  }

  /**
   * 修改物资
   * PUT /api/materials
   */
  @PutMapping("/edit")
  public Result<Void> post(@RequestBody @Validated MaterialUpdateDTO dto) {
    materialService.updateMaterial(dto);
    return Result.success(null, "修改物资成功");
  }

  /**
   * 删除物资（逻辑删除）
   * DELETE /api/materials/{id}
   */
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    materialService.deleteMaterial(id);
    return Result.success(null, "删除物资成功");
  }

  /**
   * 查询物资详情
   * GET /api/materials/{id}
   */
  @GetMapping("/{id}")
  public Result<MaterialVO> detail(@PathVariable Long id) {
    return Result.success(materialService.getMaterialById(id));
  }

  /**
   * 分页查询物资列表
   * GET /api/materials/page?pageNum=1&pageSize=10&keyword=&category=
   */
  @GetMapping("/page")
  public Result<Page<MaterialVO>> page(
          @RequestParam(defaultValue = "1") long pageNum,
          @RequestParam(defaultValue = "10") long pageSize,
          @RequestParam(required = false) String keyword,
          @RequestParam(required = false) String category) {
    return Result.success(materialService.getMaterialPage(pageNum, pageSize, keyword, category));
  }
}
