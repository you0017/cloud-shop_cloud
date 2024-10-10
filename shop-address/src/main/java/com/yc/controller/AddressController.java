package com.yc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.Address;
import com.yc.context.BaseContext;
import com.yc.mapper.AddressMapper;
import com.yc.model.JsonModel;
import com.yc.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

//@WebServlet("/html/address.action")
@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private AddressService addressService;

    /**
     * 读取默认地址
     */
    @GetMapping("/getDefault")
    public JsonModel getDefault() {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Address> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Address::getUser_id,userId).eq(Address::getIs_default,1);
        Address address = addressMapper.selectOne(lambdaQueryWrapper);

        return JsonModel.ok().setDate(address);
    }

    /**
     * 修改地址
     */
    @PutMapping("/modifyAddress")
    public JsonModel modifyAddress(@RequestBody Address address) {
        addressService.modifyAddress(address);
        return JsonModel.ok();
    }

    /**
     * 新增地址
     */
    @PutMapping("/addAddress")
    public JsonModel addAddress(@RequestBody Address address) {
        addressService.addAddress(address);
        return JsonModel.ok();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/deleteAddress")
    public JsonModel deleteAddress(@RequestParam("id") String id) {
        addressService.deleteAddress(id);

        return JsonModel.ok();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/setDefault")
    public JsonModel setDefault(@RequestParam("id") String id) {
        addressService.setDefault(id);

        return JsonModel.ok();
    }

    /**
     * 获取地址
     */
    @GetMapping("/getAllAddress")
    public JsonModel getAllAddress() {
        String id = BaseContext.getCurrentId();

        LambdaQueryWrapper<Address> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Address::getUser_id,id);
        List<Address> addresses = addressMapper.selectList(lambdaQueryWrapper);

        return JsonModel.ok().setDate(addresses);
    }
}
