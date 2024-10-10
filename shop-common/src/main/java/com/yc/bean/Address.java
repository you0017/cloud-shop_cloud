package com.yc.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer user_id;    //用户id
    private String province;    //省
    private String city;        //市
    private String town;        //区
    private String mobile;      //电话号码
    private String street;      //详细地址
    private String contact;     //联系人
    private String is_default;  //是否默认
    private String notes;   //备注
}
