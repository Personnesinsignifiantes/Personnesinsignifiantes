package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);

    public SetmealDto getByIdWithDishes(Long id);

    public void updateWithDishes(SetmealDto setmealDto);

    public void updateSetmealStatus(Integer st, List<Long> ids);
}
