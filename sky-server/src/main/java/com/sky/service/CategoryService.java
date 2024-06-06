package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    List<Category> list(CategoryDTO categoryDTO);

    void add(CategoryDTO categoryDTO);

    void update(CategoryDTO categoryDTO);

    void status(Integer status, CategoryDTO categoryDTO);

    void delete(CategoryDTO categoryDTO);
}
