package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long ids);
}
