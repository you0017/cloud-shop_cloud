package com.yc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseResult {
    private Integer code;
    private String msg;
    private Object obj;

    public static ResponseResult ok(String message){
        return ResponseResult.builder().code(1).msg(message).build();
    }

    public static ResponseResult ok(){
        return ResponseResult.builder().code(1).msg("成功").build();
    }

    public static ResponseResult error(String message){
        return ResponseResult.builder().code(0).msg(message).build();
    }

    public static ResponseResult error(){
        return ResponseResult.builder().code(0).msg("失败").build();
    }

    public <T> ResponseResult setDate(T obj){
        this.obj = obj;
        return this;
    }
}
