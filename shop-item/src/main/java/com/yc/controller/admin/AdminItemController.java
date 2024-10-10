package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.api.UserInformationClient;
import com.yc.bean.DataModel;
import com.yc.bean.DataRecord;
import com.yc.bean.Item;
import com.yc.mapper.DataRecordMapper;
import com.yc.mapper.ItemMapper;
import com.yc.utils.AliOSSUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
//@WebServlet("/admin/item.action")
@RestController
@RequestMapping("/item/admin")
public class AdminItemController {
    String fossUrl = "";  // 主图
    String fossUrl1 = ""; // 附图

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private DataRecordMapper dataRecordMapper;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private UserInformationClient userInformationClient;
    @Autowired
    private RedisTemplate redisTemplate;

    // 联动产品名
    @RequestMapping("/getMyName")
    public DataModel getMyName(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        LambdaQueryWrapper<Item> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(Item::getName);

        List<Map<String, Object>> maps = itemMapper.selectMaps(lambdaQueryWrapper);

        ud.setData(maps);
        ud.setCode(0);
        return ud;
    }


    // 规格联动拿到配置
    @RequestMapping("/getConfig")
    public DataModel getConfig(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String cityValue = request.getParameter("cityValue");   // 配置

        LambdaQueryWrapper<DataRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DataRecord::getRecorde_name, cityValue).eq(DataRecord::getRecorde_status, 1);
        List<Map<String, Object>> maps = dataRecordMapper.selectMaps(lambdaQueryWrapper);

        ud.setData(maps);
        ud.setCode(0);
        return ud;
    }


    // 规格联动拿到品牌
    @RequestMapping("/getBrand")
    public DataModel getBrand(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String Brand = request.getParameter("provinceValue");


        LambdaQueryWrapper<DataRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DataRecord::getRecorde_name, Brand).eq(DataRecord::getRecorde_status, 1);
        List<Map<String, Object>> maps = dataRecordMapper.selectMaps(lambdaQueryWrapper);

        ud.setData(maps);
        ud.setCode(0);
        System.out.println("getBrand");
        return ud;
    }

    // 规格联动拿到种类
    @RequestMapping("/getAllKind")
    public DataModel getAllKind(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        String sql = "select * from datarecord where recorde_name = '种类' and recorde_status=1";
        LambdaQueryWrapper<DataRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DataRecord::getRecorde_name, "种类").eq(DataRecord::getRecorde_status, 1);
        List<Map<String, Object>> maps = dataRecordMapper.selectMaps(lambdaQueryWrapper);

        ud.setData(maps);
        ud.setCode(0);
//        System.out.println("getAllKind");
        return ud;
    }


    // 提取路径
    public String extractImagePath(String html) {
        String pattern = "<img[^>]*src=\"([^\"]*)\"[^>]*>";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    // 上传图片
    @RequestMapping("/pic")
    public Map pic(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("pic");
        String picUrl = null;
        try {
            List<Part> parts = (ArrayList) request.getParts();
            picUrl = aliOSSUtils.upload(parts.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map map = new HashMap();
        map.put("uploaded", "1");
        map.put("url", picUrl);
        return map;
    }

    // 全部上架
    @RequestMapping("/allRise")
    public DataModel allRise(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        LambdaUpdateWrapper<Item> db = new LambdaUpdateWrapper<>();
        db.set(Item::getStatus, 1);
        int count = itemMapper.update(null, db);

        if (count > 0) {
            ud.setCode(0);
            ud.setData("成功");
            ud.setMsg("全部上架成功");
            redisTemplate.delete("hot");
        } else {
            ud.setCode(1);
            ud.setData("失败");
            ud.setMsg("全部上架失败");
        }
        return ud;
    }

    // 全部下架
    @RequestMapping("/allDel")
    public DataModel allDel(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        LambdaUpdateWrapper<Item> db = new LambdaUpdateWrapper<>();
        db.set(Item::getStatus, 0);
        int count = itemMapper.update(null, db);

        if (count > 0) {
            ud.setCode(0);
            ud.setData("成功");
            ud.setMsg("全部下架成功");
            redisTemplate.delete("hot");
        } else {
            ud.setCode(1);
            ud.setData("失败");
            ud.setMsg("全部下架失败");
        }
        return ud;
    }

    // 查询功能
    @RequestMapping("/fuzzyQuery")
    public DataModel fuzzyQuery(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String id = request.getParameter("id");
        String name = request.getParameter("name");  // 名字
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        List<Object> params = new ArrayList<>();

        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();

// 处理 id
        if (id != null && !id.trim().isEmpty()) {
            queryWrapper.eq(Item::getId, id.trim());
        }

// 处理 name
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Item::getName, name.trim());
        }

// 处理时间范围
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            queryWrapper.between(Item::getCreate_time, startTime.trim(), endTime.trim());
        } else if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.gt(Item::getCreate_time, startTime.trim());
        } else if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.lt(Item::getCreate_time, endTime.trim());
        }

// 执行查询
        List<Item> select = itemMapper.selectList(queryWrapper);

        int total = select.size();
        if (select != null && select.size() > 0) {
            ud.setData(select);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("无您想查的数据");
        }
        return ud;
    }

    // 上传副图
    @RequestMapping("/uploadSeveral")
    public void uploadSeveral(HttpServletRequest request, HttpServletResponse response) {
//        DataModel pd = new DataModel();  // 写出图片传输的结果
//        OssUtils ossUtils = new OssUtils();
//        Part filePart = request.getPart("file");  // 拿到前端的图片数据
//        String name = filePart.getSubmittedFileName();   // 提交的名字
//        name = "item/picture/" + name;
//        InputStream inputStream = filePart.getInputStream();  // 输出流
//        String itemOssUrl = "https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/";
//        Boolean result = ossUtils.UploadImageToOSS( name, inputStream); // 上传到oss的结果
//        if ( result){
//            this.fossUrl = itemOssUrl+name;
//            pd.setCode(0);
//        }
//        writeJson(pd, response);
    }

    // 上传单张item图片
    @RequestMapping("/uploadInst")
    public DataModel uploadInst(HttpServletRequest request, HttpServletResponse response) {
        DataModel pd = new DataModel();  // 写出图片传输的结果

        String temp = null;
        int flag = 1;
        try {
            Part filePart = request.getPart("file");  // 拿到前端的图片数据
            temp = aliOSSUtils.upload(filePart);
        }catch (Exception e){
            flag = 0;
        }

        if (flag==1) {
            pd.setData(temp);
            pd.setCode(0);
        }else {
            pd.setCode(1);
            pd.setMsg("上传失败");
        }
        return pd;
    }

    // 添加产品
    @RequestMapping("/addProduct")
    public DataModel addProduct(HttpServletRequest request, HttpServletResponse response) {
        DataModel dm = new DataModel();
        String name = request.getParameter("name");  // 名称
        String price = request.getParameter("price");   // 价格
        String stock = request.getParameter("stock");  // 库存
        String category = request.getParameter("category");  // 类别
        String brand = request.getParameter("brand");  // 品牌
        String spec = request.getParameter("spec");  // 描述
        String status = request.getParameter("status");  // 状态
        String pic = request.getParameter("pic");  // 图片地址
        String item_details = request.getParameter("editor1");   // 商品描述

        Item item = Item.builder()
                .name(name)
                .price(Double.parseDouble(price))
                .stock(Integer.parseInt(stock))
                .category(category)
                .brand(brand)
                .spec(spec)
                .image(pic)
                .status(Integer.parseInt(status))
                .item_details(item_details)
                .build();
        int i = itemMapper.insert(item);

        if (i >= 1) {
            dm.setCode(0);
            dm.setMsg("添加成功");
        } else {
            dm.setCode(1);
            dm.setMsg("添加失败");
        }
        return dm;
    }


    // 产品修改功能  附图不知道咋弄
    @RequestMapping("/edit")
    public DataModel edit(HttpServletRequest request, HttpServletResponse response) {
        DataModel dm = new DataModel();
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String price = request.getParameter("price");
        String status = request.getParameter("status");
        String stock = request.getParameter("stock");
        String pic = request.getParameter("pic");  // 图片地址


        LambdaUpdateWrapper<Item> updateWrapper = new LambdaUpdateWrapper<>();

// 处理要更新的字段
        if (name != null && !name.trim().isEmpty()) {
            updateWrapper.set(Item::getName, name.trim());
        }
        if (price != null && !price.trim().isEmpty()) {
            updateWrapper.set(Item::getPrice, price.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            updateWrapper.set(Item::getStatus, status.trim());
        }
        if (stock != null && !stock.trim().isEmpty()) {
            updateWrapper.set(Item::getStock, stock.trim());
        }
        if (pic != null && !pic.trim().isEmpty()) {
            updateWrapper.set(Item::getImage, pic.trim());
        }

// 确保 id 是有效的，并设置更新条件
        if (id != null && !id.trim().isEmpty()) {
            updateWrapper.eq(Item::getId, id.trim());
        }

// 执行更新
        int result = itemMapper.update(null, updateWrapper);



        if (result >= 1) {
            dm.setCode(0);
            dm.setMsg("item编辑更新成功");
        } else {
            dm.setCode(1);
        }

//        if ( this.fossUrl!="" ){  // 主图
//            String imageSql = "update item set image = ? where id = ?";
//            db.doUpdate(imageSql, this.fossUrl, id);
//            this.fossUrl="";
//        }

//        if ( this.fossUrl1!=""){
//            String imageSql = "update itempic set image = ? where itemid = ?";
//            db.doUpdate(imageSql, this.fossUrl1, id);
//            this.fossUrl1="";
//        }
        return dm;
    }


    @RequestMapping("/consoleInit")
    public DataModel consoleInit(HttpServletRequest request, HttpServletResponse response) {
        DataModel dm = new DataModel();

        String pamar = request.getParameter("param");

        if ("income".equals(pamar)) {
            dm.setCode(0);
            dm.setMsg("20000");
        } else if ("goods".equals(pamar)) {
            Long goodsTotal = itemMapper.selectCount(null);
            dm.setCode(0);
            dm.setMsg(String.valueOf(goodsTotal));
        } else if ("user".equals(pamar)) {

            Long userTotal = userInformationClient.selectCount();

            dm.setMsg(String.valueOf(userTotal));
            dm.setCode(0);
        } else {
            dm.setMsg("xxx");
            dm.setCode(0);
        }

        return dm;
    }


    // 批量删除
    @RequestMapping("/batchDelete")
    public DataModel batchDelete(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");

        LambdaUpdateWrapper<Item> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Item::getId, strArray);
        int i = itemMapper.delete(updateWrapper);

        if (i >= 1) {
            ud = allItemData(ud, request);
            ud.setCode(0);
            redisTemplate.delete("hot");
        } else {
            ud.setMsg("删除失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 删除一个
    @RequestMapping("/deleteById")
    public DataModel deleteById(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");

        int i = itemMapper.deleteById(idsStr);

        if (i >= 1) {
            ud = allItemData(ud, request);
            ud.setCode(0);
            redisTemplate.delete("hot");
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 批量停用
    @RequestMapping("/batchDisabled")
    public DataModel batchDisabled(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
//        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");

        LambdaUpdateWrapper<Item> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Item::getStatus, 0)
                .in(Item::getId, strArray);
        int i = itemMapper.update(null, updateWrapper);

        if (i >= 1) {
            ud = allItemData(ud, request);
            redisTemplate.delete("hot");
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }


    // 设置启用多个
    @RequestMapping("/batchEnabled")
    public DataModel batchEnabled(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
//        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");


        LambdaUpdateWrapper<Item> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Item::getStatus, 1)
                .in(Item::getId, strArray);
        int i = itemMapper.update(null, updateWrapper);


        if (i >= 1) {
            ud = allItemData(ud, request);
            redisTemplate.delete("hot");
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 商品初始数据获取
    @RequestMapping("/getAllItemData")
    public DataModel getAllItemData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        ud = allItemData(ud, request);
        return ud;
    }

    // getAllItemData
    // 获取所有商品数据的方法
    private DataModel allItemData(DataModel ud, HttpServletRequest request) {

        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = itemMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.last("limit " + limit + " offset " + skip);
        List<Item> limitMaps = itemMapper.selectList(queryWrapper);

//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }


    // 传到oss的方法
    /*public static boolean uploadUtil(HttpServletRequest request) throws Exception {
        DataModel pd = new DataModel();  // 写出图片传输的结果
        OssUtils ossUtils = new OssUtils();
        Part filePart = request.getPart("file");  // 拿到前端的图片数据
        String name = filePart.getSubmittedFileName();   // 提交的名字
        name = "item/picture" + name;
        InputStream inputStream = filePart.getInputStream();  // 输出流
        String itemOssUrl = "https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/";
        Boolean result = ossUtils.UploadImageToOSS(name, inputStream); // 上传到oss的结果
        return result;
    }*/


}
