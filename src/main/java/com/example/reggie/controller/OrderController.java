package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.entity.*;
import com.example.reggie.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    public OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

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


    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        log.info(orders.toString());
        //设置用户id 指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        //得到订单id
        Long ordersId = orders.getId();
        LambdaQueryWrapper<Orders> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId,ordersId);
        //根据订单id得到订单元素
        Orders one = orderService.getOne(queryWrapper);
        //得到订单表中的number 也就是订单明细表中的order_id
        String number = one.getNumber();

        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrderId,number);
        //根据订单明细表的order_id得到订单明细集合
        List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
        //新建购物车集合
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        //通过用户id把原来的购物车给清空
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        //遍历订单明细集合,将集合中的元素加入购物车集合
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            //得到菜品id或套餐id
            Long dishId = orderDetail.getDishId();
            Long setmealId = orderDetail.getSetmealId();
            //添加购物车部分属性
            shoppingCart.setUserId(currentId);
            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCart.setNumber(orderDetail.getNumber());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            if(dishId!=null){
                //订单明细元素中是菜品
                LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
                dishLambdaQueryWrapper.eq(Dish::getId,dishId);
                //根据订单明细集合中的单个元素获得单个菜品元素
                Dish dishone = dishService.getOne(dishLambdaQueryWrapper);
                //根据菜品信息添加购物车信息
                shoppingCart.setDishId(dishId);
                shoppingCart.setName(dishone.getName());
                shoppingCart.setImage(dishone.getImage());
                //调用保存购物车方法
                shoppingCarts.add(shoppingCart);
            }
            else if(setmealId!=null){
                //订单明细元素中是套餐
                LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
                setmealLambdaQueryWrapper.eq(Setmeal::getId,setmealId);
                //根据订单明细集合中的单个元素获得单个套餐元素
                Setmeal setmealone = setmealService.getOne(setmealLambdaQueryWrapper);
                //根据套餐信息添加购物车信息
                shoppingCart.setSetmealId(setmealId);
                shoppingCart.setName(setmealone.getName());
                shoppingCart.setImage(setmealone.getImage());
                //调用保存购物车方法
                shoppingCarts.add(shoppingCart);
            }
        }
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("操作成功");
    }
}
