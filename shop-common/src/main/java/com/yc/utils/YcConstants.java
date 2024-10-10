package com.yc.utils;

/**
 * 系统的常量，枚举配置
 * @program: res
 * @DESCRIPTION:
 * @author: zy
 * @create: 2021-06-30
 */
public class YcConstants {

    /**
     * 存redis用户状态的
     * xxx+session => userid
     */
    public static final String SHOP_USERID="shop_userid_";

    /**
     * 用户购物车
     * xxx+userid : cart
     */
    public static final String CARTITEMS="cartItems_";

    /**
     * 用户历史记录
     * 存储格式是  历史记录_用户id: 访问时间戳 :商品id
     *              key     :   score   :value
     */
    public static final String HISTORY="history_";

    /**
     * 短信验证码
     */
    public static final String MESSAGE="message_";

    /**
     * 用户和商家对话
     */
    public static final String CHAT="chat_";

    /**
     * 一个用户可以点赞多个评论
     */
    public static final String LIKES_COMMENT="_likes_commentToUser_";

    /**
     * 一个评论可以被多个用户点赞
     */
    public static final String LIKES_USER = "_likes_userToComment_";

    /**
     * 点踩类似
     */
    public static final String DISLIKES_COMMENT="_dislikes_commentToUser_";

    public static final String DISLIKES_USER = "_dislikes_userToComment_";

    /**
     * 放入redis的优惠券
     */
    public static final String GRAB_COUPON="coupon_";
    /**
     * 优惠券对应的锁
     */
    public static final String COUPON_LOCK="coupon_lock_";
    /**
     * 优惠券对应的用户
     */
    public static final String GRAB_COUPON_USER="grab_coupon_user_";
    /**
     * 用户对应的优惠券
     */
    public static final String USER_COUPON_GRAB="user_coupon_grab_";
    /**
     * 优惠券和用户统一写入数据库的锁
     */
    public static final String COUPON_LOCK_SQL="coupon_lock_sql_";
}
