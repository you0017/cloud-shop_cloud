package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.Address;

public interface AddressService extends IService<Address> {
    public void setDefault(String id);

    public void deleteAddress(String id);

    public void addAddress(Address address);

    public void modifyAddress(Address address);
}
