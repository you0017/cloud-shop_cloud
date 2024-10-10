package com.yc.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
// 数据字典
@TableName("datarecord")
@Builder
public class DataRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(value = "recorde_name")
    private String recorde_name;  // 键
    @TableField(value = "recorde_value")
    private String recorde_value;  // 值
    @TableField(value = "recorde_status")
    private Integer recorde_status; // 是否启用
}
