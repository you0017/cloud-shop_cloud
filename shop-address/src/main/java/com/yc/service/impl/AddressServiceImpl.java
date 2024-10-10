package com.yc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.bean.Address;
import com.yc.context.BaseContext;
import com.yc.mapper.AddressMapper;
import com.yc.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public void modifyAddress(Address address) {

        addressMapper.updateById(address);
        //设置默认地址
        setDefault(String.valueOf(address.getId()));
    }

    @Override
    public void addAddress(Address address) {



        String userId = BaseContext.getCurrentId();
        address.setUser_id(Integer.valueOf(userId));
        //如果是默认地址，其他就设置为非默认
        if (address.getIs_default().equals("1")){
            this.lambdaUpdate().set(Address::getIs_default,"0").eq(Address::getUser_id,userId).update();
        }
        this.save(address);
    }

    @Override
    public void deleteAddress(String id) {
        String userId = BaseContext.getCurrentId();
        this.lambdaUpdate().eq(Address::getId,id).eq(Address::getUser_id,userId).remove();
    }

    /**
     * 设置默认地址
     */
    @Override
    public void setDefault(String id) {

        String userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Address> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Address::getUser_id,userId);
        List<Address> addresses = addressMapper.selectList(lambdaQueryWrapper);
        //除了id 为当前id的，其他设置为非默认
        for (Address address : addresses) {
            if (!address.getId().equals(Integer.valueOf(id))){
                address.setIs_default("0");
            }else{
                address.setIs_default("1");
            }
        }
        this.updateBatchById(addresses);
    }
}
