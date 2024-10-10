package com.yc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JsonModel implements Serializable {
    private Integer code ;//响应码  0表示失败   1表示成功
    private Object obj;
    private String error;


    public static JsonModel ok(String message){
        return JsonModel.builder().code(1).error(message).build();
    }

    public static JsonModel ok(){
        return JsonModel.builder().code(1).error("成功").build();
    }

    public static JsonModel error(String message){
        return JsonModel.builder().code(0).error(message).build();
    }

    public static JsonModel error(){
        return JsonModel.builder().code(0).error("失败").build();
    }

    public <T> JsonModel setDate(T obj){
        this.obj = obj;
        return this;
    }
}
