package com.yc.api;

import com.yc.bean.DataRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("shop-system")
public interface FrontEditClient {

    @PostMapping("/frontEdit/admin/add")
    public int add(@RequestBody DataRecord dataRecord);

    @GetMapping("/frontEdit/admin/get")
    public List<Map<String, Object>> get();
}
