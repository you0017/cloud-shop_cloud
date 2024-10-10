package com.yc.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Item item;//商品
    private Double smallCount;//小计
    private Integer num=0;//数量

    public Double getSmallCount() {
        if (this.item != null){
            smallCount = this.num * this.item.getPrice();
        }
        return smallCount;
    }

}
