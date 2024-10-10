package com.yc.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataModel implements Serializable {
    private Integer code ;//响应码  0表示成功   1表示失败
    private Object data;  // 返回数据
    private String msg; // 信息
    private Integer count;
}
