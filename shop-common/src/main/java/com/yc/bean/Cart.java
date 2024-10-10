package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    List<CartItem> cartItems;
    private Double total;//总价
    private Integer num;//商品数量

    public Double getTotal() {
        total = 0.0;
        for (CartItem cartItem : cartItems) {
            total += cartItem.getSmallCount();
        }
        return total;
    }
}
