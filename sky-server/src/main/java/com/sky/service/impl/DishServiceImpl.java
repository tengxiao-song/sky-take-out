package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional // 同时处理两个表的插入操作保证atomicity
    public void saveWithFlavor(DishDTO dishDTO) {
        // 插入菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 获取insert语句的自增主键（回显）
        Long dishId = dish.getId();

        // 插入菜品口味表
        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if (dishFlavorList != null && dishFlavorList.size() > 0) {
            // 设置dishId
            for (DishFlavor dishFlavor : dishFlavorList) {
                dishFlavor.setDishId(dishId);
            }
            // 批量插入，动态sql foreach
            dishFlavorMapper.insertBatch(dishFlavorList);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 返回的是Page对象泛形DishVO，DishVO里的categoryName是Dish pojo没有的需要sql join获得
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        // 判断是否启售中，如果启售中则不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断菜品是否被套餐关联，如果被套餐关联则不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品表中的数据，级联删除菜品口味表中的数据
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavor = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 先删除原有的口味
        dishFlavorMapper.deleteByDishId(dish.getId());
        // 再插入新的口味
        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if (dishFlavorList != null && dishFlavorList.size() > 0) {
            for (DishFlavor dishFlavor : dishFlavorList) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavorList);
        }
    }
}
