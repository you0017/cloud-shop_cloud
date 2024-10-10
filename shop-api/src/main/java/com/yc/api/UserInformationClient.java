package com.yc.api;

import com.yc.bean.UserInformation;
import com.yc.model.JsonModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shop-user")
public interface UserInformationClient {

    @GetMapping("/user/getById")
    public UserInformation getById(@RequestParam("id") Integer id);

    @GetMapping("/user/selectCount")
    public Long selectCount();

    @GetMapping("/user/getIdByAccountName")
    public UserInformation getIdByAccountName(@RequestParam("sender") String sender);
}
