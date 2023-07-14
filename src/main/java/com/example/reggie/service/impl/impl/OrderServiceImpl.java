package com.example.reggie.service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.CustomException;
import com.example.reggie.common.R;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.entity.*;
import com.example.reggie.mapper.OrderMapper;
import com.example.reggie.service.impl.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.invoke.LambdaConversionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Transactional
    public void submit(Orders orders) {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null||shoppingCarts.size() == 0){
            throw new CustomException("购物车为空");
        }

        User user = userService.getById(currentId);

        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null){
            throw new CustomException("地址为空");
        }

        long id = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails=shoppingCarts.stream().map((item) ->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(id);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(id));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);

        orderDetailService.saveBatch(orderDetails);

        shoppingCartService.remove(queryWrapper);
    }

    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        Page<Orders> pageInfo=new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageInfo,queryWrapper);
        LambdaQueryWrapper<OrderDetail> Wrapper = new LambdaQueryWrapper<>();
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list=records.stream().map((item) ->{
            OrdersDto ordersDto = new OrdersDto();
            Long orderId = item.getId();
            Wrapper.eq(OrderDetail::getOrderId,orderId);
            BeanUtils.copyProperties(item,ordersDto);
            return ordersDto;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        dtoPage.setRecords(list);
        return dtoPage;
    }


}
