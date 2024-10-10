package com.yc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.bean.Item;
import com.yc.bean.ItemDoc;
import com.yc.bean.OrderDetail;
import com.yc.bean.PageBean;
import com.yc.mapper.ItemMapper;
import com.yc.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    private final ItemMapper itemMapper;
    private final RestHighLevelClient client;

    @Override
    public List<Item> getCategories() {
        QueryWrapper<Item> queryWrapper = Wrappers.query();
        queryWrapper.select("DISTINCT category");

        List<String> list = this.listObjs(queryWrapper, Object::toString);
        List<Item> items = new ArrayList<>();
        for (String s : list) {
            Item item = new Item();
            item.setCategory(s);
            items.add(item);
        }
        return items;
    }

    @Override
    public List<Item> getBrands() {
        QueryWrapper<Item> queryWrapper = Wrappers.query();
        queryWrapper.select("DISTINCT category");
        List<String> list = this.listObjs(queryWrapper, Object::toString);
        List<Item> items = new ArrayList<>();
        for (String s : list) {
            Item item = new Item();
            item.setBrand(s);
            items.add(item);
        }
        return items;
    }

    @Override
    public PageBean<Item> selectByPage(PageBean<Item> pageBean) throws IOException {
        if (pageBean.getPageno()==0){
            pageBean.setPageno(1);
        }
        GetRequest get = null;
        //计算起始位置和结束位置
        int skip = (pageBean.getPageno() - 1) * pageBean.getPagesize() + 1;
        int end = skip + pageBean.getPagesize();
        List<Item> data = new ArrayList<>();
        for (int result = 0; result < pageBean.getPagesize(); result++) {
            get = new GetRequest("shop_items", String.valueOf((skip+result)));

            GetResponse documentFields = client.get(get, RequestOptions.DEFAULT);
            if (documentFields.isExists()){
                String sourceAsString = documentFields.getSourceAsString();
                ItemDoc bean = JSONUtil.toBean(sourceAsString, ItemDoc.class);
                Item item = BeanUtil.copyProperties(bean, Item.class);
                data.add(item);
            }
        }
        //如果ES查不到就去数据库查
        if (data==null || data.size()<=0){
            // 创建 MyBatis-Plus 分页对象
            Page<Item> page = new Page<>(pageBean.getPageno(), pageBean.getPagesize());

            // 创建查询条件
            QueryWrapper<Item> queryWrapper = new QueryWrapper<>();

            // 添加搜索条件（如果有的话）
            if (pageBean.getSearch() != null && !pageBean.getSearch().isEmpty()) {
                queryWrapper.like("name", pageBean.getSearch())
                        .or()
                        .like("category", pageBean.getSearch());
            }

            // 添加排序条件（如果有的话）
            if (pageBean.getSortby() != null && !pageBean.getSortby().isEmpty()) {
                if ("asc".equalsIgnoreCase(pageBean.getSort())) {
                    queryWrapper.orderByAsc(pageBean.getSortby());
                } else {
                    queryWrapper.orderByDesc(pageBean.getSortby());
                }
            }
            queryWrapper.eq("status",1);
            // 执行分页查询
            this.page(page, queryWrapper);

            // 设置分页结果到 PageBean
            pageBean.setTotal(page.getTotal()); // 设置总记录数
            pageBean.setDataset(page.getRecords()); // 设置当前页记录
            pageBean.setTotalpages((int) Math.ceil((double) page.getTotal() / pageBean.getPagesize())); // 计算总页数
            pageBean.setPre(pageBean.getPageno() > 1 ? pageBean.getPageno() - 1 : 1); // 上一页
            pageBean.setNext(pageBean.getPageno() < pageBean.getTotalpages() ? pageBean.getPageno() + 1 : 0); // 下一页
        }else{
            Long count = itemMapper.selectCount(null);
            pageBean.setDataset(data);
            pageBean.setTotal(data.size());
            pageBean.setTotalpages((int) Math.ceil((double) count / pageBean.getPagesize()));
            pageBean.setPre(pageBean.getPageno() > 1 ? pageBean.getPageno() - 1 : 0); // 上一页
            pageBean.setNext(pageBean.getPageno() < pageBean.getTotalpages() ? pageBean.getPageno() + 1 : 0); // 下一页
            return pageBean;
        }

        //ES插入
        List<Item> dataset = pageBean.getDataset();
        for (Item item : dataset) {
            //获取数据库数据封装为ES数据
            ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
            //1.准备Request
            IndexRequest request = new IndexRequest("shop_items").id(itemDoc.getId());
            //2.准备请求参数
            request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
            //3.发送请求
            client.index(request, RequestOptions.DEFAULT);
        }


        return pageBean;
    }

    /**
     * 回退库存
     * @param orderDetails
     */
    @Transactional
    @Override
    public void fallback(List<OrderDetail> orderDetails) {
        List<Integer> ids = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ids.add(orderDetail.getItem_id());
        }
        List<Item> items = itemMapper.selectBatchIds(ids);
        // 数量加回去
        for (Item item : items) {
            item.setStock(item.getStock()+orderDetails.stream().filter(orderDetail -> orderDetail.getItem_id().equals(item.getId())).map(OrderDetail::getNum).collect(Collectors.summingInt(Integer::intValue)));
        }
        // 更新库存
        this.updateBatchById(items);
    }
}
