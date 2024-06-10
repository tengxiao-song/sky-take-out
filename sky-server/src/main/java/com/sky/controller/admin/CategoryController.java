package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result list(CategoryDTO categoryDTO) {
        List<Category> list = categoryService.list(categoryDTO.getType());
        return Result.success(list);
    }

    @PostMapping
    public Result add(@RequestBody CategoryDTO categoryDTO) {
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result enableOrDisable(@PathVariable Integer status, CategoryDTO categoryDTO) {
        categoryService.status(status, categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(CategoryDTO categoryDTO) {
        categoryService.delete(categoryDTO);
        return Result.success();
    }
}
