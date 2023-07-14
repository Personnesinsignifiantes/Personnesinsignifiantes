package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);


    public void updateWithFlavor(DishDto dishDto);


    public void remove(List<Long> ids);


}
