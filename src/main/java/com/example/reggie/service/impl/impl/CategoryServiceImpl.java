package com.example.reggie.service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.mapper.CategoryMapper;
import com.example.reggie.service.impl.CategoryService;
import com.example.reggie.service.impl.DishService;
import com.example.reggie.service.impl.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishlambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishlambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count1 = dishService.count(dishlambdaQueryWrapper);

        if (count1 > 0){
            throw new CustomException("关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0){
            throw new CustomException("关联了套餐，不能删除");
        }

        super.removeById(ids);
    }
}
