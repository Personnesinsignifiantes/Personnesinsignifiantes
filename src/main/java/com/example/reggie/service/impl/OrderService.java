package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    public void submit(Orders orders);

    public Page<OrdersDto> userPage(int page, int pageSize);
}
