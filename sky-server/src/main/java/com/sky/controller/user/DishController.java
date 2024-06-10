package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 查询redis中是否存在该分类的菜品
        String key = "dish_" + categoryId; // key为dish_categoryId, value为对应分类下的菜品(List<DishVO>)
        ValueOperations valueOperations = redisTemplate.opsForValue(); // 操作字符串
        List<DishVO> list = (List<DishVO>) valueOperations.get(key); // redis获取出来的数据可以是任何类型，需要强转
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        // 如果数据不存在，查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);

        valueOperations.set(key, list); // 将查询结果存入redis, key为dish_categoryId, value为List<DishVO>(会被序列化成redis的字符串)
        return Result.success(list);
    }
}
