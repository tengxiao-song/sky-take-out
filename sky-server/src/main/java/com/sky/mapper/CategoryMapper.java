package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Page<Category> page(CategoryPageQueryDTO categoryPageQueryDTO);

    List<Category> list(CategoryDTO categoryDTO);

    @AutoFill(OperationType.INSERT)
    @Insert("insert into category (type, name, sort, status, create_user, update_user, create_time, update_time) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    void insert(Category category);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    @Delete("delete from category where id = #{id}")
    void delete(Long id);
}
