package com.yc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.*;
import com.yc.context.BaseContext;
import com.yc.mapper.ItemMapper;
import com.yc.mapper.ItemPicMapper;
import com.yc.model.JsonModel;
import com.yc.service.ItemService;
import com.yc.utils.JwtTokenUtil;
import com.yc.utils.YcConstants;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

//@WebServlet("/html/item.action")
@RestController
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemPicMapper itemPicMapper;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 热门产品  用redis
     */
    @GetMapping("/getHot")
    public JsonModel getHot() {
        List<Item> items = (List<Item>) redisTemplate.opsForValue().get("hot");

        if (items==null||items.size()<=0){
            LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Item::getSold).eq(Item::getStatus, 1);
            items = itemMapper.selectList(wrapper);
            redisTemplate.opsForValue().set("hot",items);
        }

        return JsonModel.ok().setDate(items);
    }

    /**
     * 联想查询
     */
    @GetMapping("/association")
    public JsonModel association(@RequestParam("association") String association) throws IOException {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Item::getCategory, association)
                .or()
                .like(Item::getName, association)
                .or()
                .like(Item::getBrand, association)
                .or()
                .like(Item::getSpec, association)
                .or()
                .like(Item::getItem_details, association)
                .eq(Item::getStatus, 1)
                .orderByAsc(Item::getSold) // 使用字段名
                .last("LIMIT 6 OFFSET 0"); // MyBatis-Plus 允许直接使用 SQL 片段
        List<Item> list = itemMapper.selectList(wrapper);
        return JsonModel.ok().setDate(list);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/getCategories")
    public JsonModel getCategories() {
        List<Item> select = itemService.getCategories();


        return JsonModel.ok().setDate(select);
    }

    /**
     * 根据分类查品牌
     */
    @GetMapping("/getBrandByCategories")
    public JsonModel getBrandByCategories(@RequestParam("category") String category) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Item::getCategory, category);
        List<Item> items = itemMapper.selectList(wrapper);

        return JsonModel.ok().setDate(items);
    }
    /**
     * 获取所有品牌
     */
    @GetMapping("/getBrands")
    public JsonModel getBrands() {
        List<Item> select = itemService.getBrands();

        return JsonModel.ok().setDate(select);
    }

    /**
     * 根据id删记录
     */
    @DeleteMapping("/delHistoryById")
    public JsonModel delHistoryById(@RequestParam("id") String id) throws IOException {
        String userId = BaseContext.getCurrentId();
        long zrem = redisTemplate.opsForZSet().remove(YcConstants.HISTORY + userId, id);
        return JsonModel.ok();
    }

    /**
     * 清空历史记录
     */
    @DeleteMapping("/clearHistory")
    public JsonModel clearHistory() throws IOException {
        String user_id = BaseContext.getCurrentId();
        redisTemplate.delete(YcConstants.HISTORY+user_id);
        return JsonModel.ok();
    }
    //http://localhost/index.jsp?id=272&total=399.02
    /**
     * 获取浏览历史
     */
    @GetMapping("/getHistory")
    public JsonModel getHistory() {
        String user_id = BaseContext.getCurrentId();

        if (user_id==null){
            return JsonModel.error();
        }

        //考虑未登录
        if (user_id==null){
            return JsonModel.ok();
        }

        Set<String> zrevrange = redisTemplate.opsForZSet().reverseRange(YcConstants.HISTORY + user_id, 0, 2);
        if (zrevrange==null||zrevrange.size()<=0){
            return JsonModel.error();
        }
        List<String> history = new ArrayList<>(zrevrange);

        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Item::getId, history);
        List<Item> items = itemMapper.selectList(wrapper);
        if (items==null||items.size()<=0){
            return JsonModel.error();
        }

        return JsonModel.ok().setDate(items);
    }

    /**
     * 根据id移除购物车商品
     */
    @DeleteMapping("/removeItemById")
    public JsonModel removeItemById(@RequestParam("id") String id) {
        String user_id = BaseContext.getCurrentId();

        //购物车key
        String key = YcConstants.CARTITEMS+user_id;
        redisTemplate.opsForHash().delete(key,id);//删除这个键值对

        return JsonModel.ok();

    }

    /**
     * 根据id查商品detail
     */
    @GetMapping("/getItemById")
    public JsonModel getItemById(@RequestParam("id") String id,@RequestHeader("token") String token) {

        //如果登录了就记录一下历史记录
        if (token!=null && !token.equals("") && jwtTokenUtil.decodeJWTWithKey(token)!=null){
            Claims claims = jwtTokenUtil.decodeJWTWithKey(token);
            String user_id = String.valueOf(claims.get("id"));
            if (user_id!=null){
                //存储格式是  历史记录_用户id: 访问时间戳 :商品id
                //              key     :   score   :value
                //用sorted set
            /*不重复但是有一个用于排序的隐含列
            zadd key score1 value1 score2 value2 ...  score权重，用来排序的*/
                redisTemplate.opsForZSet().add(YcConstants.HISTORY+user_id,id,System.currentTimeMillis());
            }
        }


        Item item = itemMapper.selectById(id);
        LambdaQueryWrapper<ItemPic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemPic::getItemid, id);
        List<ItemPic> itemPics = itemPicMapper.selectList(wrapper);

        item.setItempic(itemPics);

        return JsonModel.ok().setDate(item);
    }

    /**
     * 获取购物车
     */
    @GetMapping("/getCart")
    public JsonModel getCart() {
        //先获取用户id
        String id = BaseContext.getCurrentId();

        if (id==null){
            //没登就不查，也不报错所以1
            return JsonModel.ok();
        }

        //根据id取购物车商品编号
        String key = YcConstants.CARTITEMS+id;
        //id <=> 数量
        Map<String, String> map = redisTemplate.opsForHash().entries(key);
        if (map==null||map.size()<=0){
            return JsonModel.builder().error("购物车为空").build();
        }

        //查商品   看看是否下架
        Set<String> keys = map.keySet();
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Item::getId,keys);
        List<Item> select = itemMapper.selectList(wrapper);

        //每个商品都封装成cartItem放进购物车类
        Cart cart = new Cart();
        CartItem cartItem = null;
        List<CartItem> list = new ArrayList(); //购物车中转站
        Double total = 0.0;//总价
        Integer num = 0;//总数
        for (Item item : select) {
            cartItem = new CartItem();
            cartItem.setItem(item);

            //根据id在map里面取数量
            cartItem.setNum(Integer.valueOf(map.get(item.getId())));
            num += Integer.valueOf(map.get(item.getId()));

            //计算小计
            cartItem.getSmallCount();
            //计算总价
            total = total+cartItem.getSmallCount();

            //放入购物车
            list.add(cartItem);
        }

        //购物车准备好了
        cart.setCartItems(list);
        cart.setTotal(total);
        cart.setNum(num);

        return JsonModel.ok().setDate(cart);
    }
    /**
     * 添加购物车
     */
    @PostMapping("/addCart")
    public JsonModel addCart(@RequestParam("id") String itemId) {
        //看看登录了没有
        String id =  BaseContext.getCurrentId();

        //看看库里有没有 并且没有下架
        //String sql = "select * from item where status=1 and id=? and stock>0";
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getId,itemId)
                .gt(Item::getStock,0);
        Item item = itemMapper.selectOne(wrapper);
        if (item==null){
            return JsonModel.error("商品没有了");
        }

        String key = YcConstants.CARTITEMS + id;
        //左边是商品id，右边是商品数量
        Map<String, String> map = redisTemplate.opsForHash().entries(key);//购物车商品的id:数量
        if (map==null||map.size()<=0){
            //redis没有这个购物车
            map = new HashMap<>();
            map.put(itemId,"1");
        }else{
            //有东西，看看商品在不在
            Set<Map.Entry<String, String>> entries = map.entrySet();
            int flag = 0;
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().equals(itemId)){
                    //如果有
                    map.put(itemId, String.valueOf(Integer.parseInt(entry.getValue())+1));//数量+1
                    flag=1;
                    break;
                }
            }
            if (flag==0){
                //遍历完了也没找到
                map.put(itemId,"1");
            }
            flag=0;
        }


        //重新
        redisTemplate.opsForHash().putAll(key,map);

        return JsonModel.ok();
    }

    /**
     * 分页查询 可选 by price 或 rating
     */
    @PostMapping("/selectByPage")
    public JsonModel selectByPage(@RequestBody PageBean<Item> pageBean) throws IOException {
        //分页查询
        pageBean = itemService.selectByPage(pageBean);

        return JsonModel.ok().setDate(pageBean);
    }
    /**
     * 查看所有商品
     */
    @GetMapping("/selectAllItems")
    protected JsonModel selectAllItems() {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus,1);
        List<Item> select = itemMapper.selectList(wrapper);
        return JsonModel.ok().setDate(select);
    }

    /**
     * 回退取消订单的商品数量
     */
    @PutMapping("/fallback")
    public void fallback(@RequestBody List<OrderDetail> orderDetails) {
        itemService.fallback(orderDetails);
    }

    /**
     * 根据ids查订单中所有的商品
     */
    @GetMapping("/getByIds")
    public List<Item> getByIds(@RequestParam("ids") List<String> ids){
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Item::getId,ids);
        return itemMapper.selectList(wrapper);
    }

}
