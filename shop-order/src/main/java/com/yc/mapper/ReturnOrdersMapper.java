package com.yc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.bean.AdminReturnOrder;
import com.yc.bean.ReturnOrder;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReturnOrdersMapper extends BaseMapper<ReturnOrder> {
    @Select("SELECT  r.product_id, r.return_id, r.tracking_number, r.order_id, r.product_name, r.refund_amount, u.name, " +
            "r.return_date, r.back_type, r.return_status, r.return_reason,  r.return_quantity " +
            "FROM returnorders r JOIN userinformation u ON r.customer_id = u.id WHERE r.return_status in ( 1, 5) " +
            " ORDER BY r.return_status ASC limit #{limit} offset #{skip}")
    public List<AdminReturnOrder> getGoodsAndMoneyData(int limit, int skip);

    @Select("SELECT r.tracking_company, r.product_id, r.return_id, r.tracking_number, r.order_id, r.product_name, r.refund_amount, u.name, r.return_date, r.back_type, " +
            " r.return_status, r.return_reason, r.return_quantity " +
            " FROM returnorders r " +
            " JOIN userinformation u ON r.customer_id = u.id " +
            " WHERE FIELD(r.return_status, 2, 3, 4) IN (1, 2, 3) " +
            " ORDER BY FIELD(r.return_status, 3, 2, 4) limit #{limit} offset #{skip} ")
    public List<AdminReturnOrder> getOnlyMoney(int limit, int skip);
}
