package cn.tycoding.scst.system.biz.service.impl;

import cn.tycoding.scst.common.core.utils.QueryPage;
import cn.tycoding.scst.system.api.dto.Tree;
import cn.tycoding.scst.system.api.entity.SysDept;
import cn.tycoding.scst.system.api.utils.TreeUtils;
import cn.tycoding.scst.system.biz.mapper.SysDeptMapper;
import cn.tycoding.scst.system.biz.service.SysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tycoding
 * @date 2019-06-02
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public List<Tree<SysDept>> tree() {
        List<SysDept> deptList = sysDeptMapper.selectList(new LambdaQueryWrapper<>());
        List<Tree<SysDept>> treeList = new ArrayList<>();
        deptList.forEach(dept -> {
            Tree<SysDept> tree = new Tree<>();
            tree.setId(dept.getId());
            tree.setParentId(dept.getParentId());
            tree.setName(dept.getName());
            treeList.add(tree);
        });
        return TreeUtils.build(treeList);
    }


    @Override
    public IPage<SysDept> list(SysDept dept, QueryPage queryPage) {
        IPage<SysDept> page = new Page<>(queryPage.getPage(), queryPage.getLimit());
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysDept::getCreateTime);
        queryWrapper.like(StringUtils.isNotBlank(dept.getName()), SysDept::getName, dept.getName());
        return sysDeptMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public void add(SysDept dept) {
        Long pId = dept.getParentId();
        if (pId == null) {
            dept.setParentId(0L);
        }
        dept.setCreateTime(new Date());
        this.save(dept);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysDeptMapper.deleteById(id);
        sysDeptMapper.changeTopNode(id);
    }

    @Override
    @Transactional
    public void update(SysDept dept) {
        this.updateById(dept);
    }

    @Override
    public boolean checkName(String name, String id) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(SysDept::getName, name);
            queryWrapper.ne(SysDept::getId, id);
        } else {
            queryWrapper.eq(SysDept::getName, name);
        }
        return sysDeptMapper.selectList(queryWrapper).size() <= 0;
    }
}
