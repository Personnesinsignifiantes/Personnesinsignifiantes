package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.entity.Orders;
import com.example.reggie.service.impl.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    public OrderService orderService;

    //手机端
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据: {}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        Page<OrdersDto> dtoPage = orderService.userPage(page,pageSize);
        return R.success(dtoPage);
    }

    //管理端
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number, String beginTime ,String endTime){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number !=null,Orders::getId,number);
        queryWrapper.eq(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.eq(endTime != null,Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        orderService.page(pageInfo,queryWrapper);
        List<Orders> records = pageInfo.getRecords();
        records= records.stream().map((item) ->{
            item.setUserName("用户"+item.getUserId());
            return item;
        }).collect(Collectors.toList());
          return R.success(pageInfo);
    }

    @PutMapping
    public R<String> updateOrder(@RequestBody Orders orders){

        orderService.updateById(orders);
        return R.success("订单派送成功");

    }
}
