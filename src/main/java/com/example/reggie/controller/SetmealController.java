package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.service.impl.CategoryService;
import com.example.reggie.service.impl.SetmealDishService;
import com.example.reggie.service.impl.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("message:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page , int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> DtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, DtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        DtoPage.setRecords(list);

        return R.success(DtoPage);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto =setmealService.getByIdWithDishes(id);
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.updateWithDishes(setmealDto);
        return R.success("修改套餐成功");
    }

    @PostMapping("/status/{st}")
    public R<String> updateStatus(@PathVariable Integer st,@RequestParam List<Long> ids){
        setmealService.updateSetmealStatus(st,ids);
        return R.success("状态修改成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }


}
