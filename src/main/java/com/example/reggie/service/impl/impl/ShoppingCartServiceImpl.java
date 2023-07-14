package com.example.reggie.service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.entity.ShoppingCart;
import com.example.reggie.mapper.SetmealDishMapper;
import com.example.reggie.mapper.ShoppingCartMapper;
import com.example.reggie.service.impl.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>implements ShoppingCartService {
}
