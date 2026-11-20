package com.rickrone.logiflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rickrone.logiflow.common.exception.BusinessException;
import com.rickrone.logiflow.mapper.BizMaterialMapper;
import com.rickrone.logiflow.model.dto.MaterialCreateDTO;
import com.rickrone.logiflow.model.dto.MaterialUpdateDTO;
import com.rickrone.logiflow.model.entity.BizMaterial;
import com.rickrone.logiflow.model.vo.MaterialVO;
import com.rickrone.logiflow.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
  private final BizMaterialMapper bizMaterialMapper;
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createMaterial(MaterialCreateDTO dto) {
    // 1. 业务校验：物资编码必须唯一
    //    - LambdaQueryWrapper 根据 material_code 生成等值条件，selectCount 只查数量
    //    - 全局逻辑删除会自动追加 deleted = 0，这里不用手写
    //    - 预检查是为了给出友好提示；并发下仍可能重复，
    //      最终由数据库唯一索引 uk_material_code 兜底
    Long count = bizMaterialMapper.selectCount(
            new LambdaQueryWrapper<BizMaterial>()
                    .eq(BizMaterial::getMaterialCode, dto.getMaterialCode())
    );
    if (count > 0) {
      throw new BusinessException("物资编码已存在：" + dto.getMaterialCode());
    }

    // 2. DTO -> Entity：把「接口入参」转换成「数据库记录」
    //    - DTO 只包含客户端允许传入的字段，Entity 对应 biz_material 表的完整一行
    //    - BeanUtils.copyProperties 按同名属性复制：
    //      materialCode / materialName / category / stock
    //    - id、version、deleted、createTime、updateTime 都不由客户端提供
    BizMaterial entity = new BizMaterial();
    BeanUtils.copyProperties(dto, entity);

    // 3. 初始化服务端字段：新物资的乐观锁版本号从 0 开始
    entity.setVersion(0);

    // 4. 落库：执行 INSERT，自增主键会回填到 entity.id
    bizMaterialMapper.insert(entity);

    // 5. 返回新记录主键，供 Controller 组装响应
    return entity.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateMaterial(MaterialUpdateDTO dto) {
    // 1. 先查出原记录（拿到最新的 version 值，供乐观锁使用）
    BizMaterial existing = bizMaterialMapper.selectById(dto.getId());
    if (existing == null) {
      throw new BusinessException("物资不存在，ID：" + dto.getId());
    }

    // 2. 更新可修改字段
    existing.setMaterialName(dto.getMaterialName());
    existing.setCategory(dto.getCategory());
    existing.setStock(dto.getStock());

    // 3. 带乐观锁更新（若并发冲突导致 rows == 0，抛出业务异常）
    int rows = bizMaterialMapper.updateById(existing);
    if (rows == 0) {
      throw new BusinessException("物资数据已被他人修改，请刷新后重试");
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteMaterial(Long id) {
    BizMaterial existing = bizMaterialMapper.selectById(id);
    if (existing == null) {
      throw new BusinessException("物资不存在，ID：" + id);
    }
    // 配置了 @TableLogic 后，deleteById 会自动执行逻辑删除（UPDATE deleted = 1）
    bizMaterialMapper.deleteById(id);
  }

  @Override
  public MaterialVO getMaterialById(Long id) {
    BizMaterial entity = bizMaterialMapper.selectById(id);
    if (entity == null) {
      throw new BusinessException("物资不存在，ID：" + id);
    }
    return toVO(entity);
  }

  private MaterialVO toVO(BizMaterial entity) {
    MaterialVO vo = new MaterialVO();
    BeanUtils.copyProperties(entity, vo);
    return vo;
  }

  @Override
  public Page<MaterialVO> getMaterialPage(long pageNum, long pageSize, String keyword, String category) {
    // 1. 准备分页参数：Page 先承载 current/size，查询后会被回填 records/total
    Page<BizMaterial> pageParam = new Page<>(pageNum, pageSize);

    // 2. 拼接查询条件（条件为空时对应片段自动跳过）
    LambdaQueryWrapper<BizMaterial> query = new LambdaQueryWrapper<>();
    // 关键字同时匹配名称和编码；and(...) 会用括号包住 OR，
    // 避免和后面的分类条件组合时出现 OR/AND 优先级错误
    if (StringUtils.hasText(keyword)) {
      query.and(w -> w.like(BizMaterial::getMaterialName, keyword)
              .or()
              .like(BizMaterial::getMaterialCode, keyword));
    }
    // 分类精确匹配
    if (StringUtils.hasText(category)) {
      query.eq(BizMaterial::getCategory, category);
    }
    // 分页需要稳定排序，按 id 倒序即“新物资在前”
    query.orderByDesc(BizMaterial::getId);

    // 3. 执行分页查询：分页插件会自动执行 COUNT + LIMIT 两条 SQL，
    //    并把 total/records 回填到 entityPage；逻辑删除条件由 MyBatis-Plus 自动追加
    Page<BizMaterial> entityPage = bizMaterialMapper.selectPage(pageParam, query);

    // 4. Entity Page -> VO Page
    //    泛型不同不能直接复用 Page，先复制分页元信息（当前页/每页条数/总数）
    Page<MaterialVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
    // 逐条 Entity -> VO，只暴露 VO 中声明的字段
    List<MaterialVO> voList = entityPage.getRecords().stream()
            .map(this::toVO)
            .toList();
    voPage.setRecords(voList);
    return voPage;
  }
}
