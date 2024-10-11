package com.yc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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
        if (pageBean.getPageno() == 0) {
            pageBean.setPageno(1);
        }

        // 创建 Elasticsearch 的搜索请求
        SearchRequest searchRequest = new SearchRequest("shop_items"); // 索引名

        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 添加状态条件
        boolQuery.must(QueryBuilders.termQuery("status", 1)); // 确保状态为 1

        // 添加搜索条件，确保满足至少一个条件
        if (pageBean.getSearch() != null && !pageBean.getSearch().isEmpty()) {
            boolQuery.should(QueryBuilders.matchQuery("name", pageBean.getSearch()))
                    .should(QueryBuilders.matchQuery("category", pageBean.getSearch()));
            boolQuery.minimumShouldMatch(1); // 至少需要匹配一个条件
        }
        searchRequest.source().query(boolQuery);

        // 添加分页
        int from = (pageBean.getPageno() - 1) * pageBean.getPagesize();
        searchRequest.source().from(from).size(pageBean.getPagesize());

        // 添加排序条件
        if (pageBean.getSortby() != null && !pageBean.getSortby().isEmpty()) {
            searchRequest.source().sort(SortBuilders.fieldSort(pageBean.getSortby()).order(SortOrder.valueOf(pageBean.getSort().toUpperCase())));
        }

        // 执行搜索查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 处理搜索结果
        List<ItemDoc> itemDocs = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            // 反序列化为 Item 对象（根据你的实际需求处理）
            ItemDoc itemDoc = JSON.parseObject(hit.getSourceAsString(), ItemDoc.class);
            itemDocs.add(itemDoc);
        }

        // ItemDoc -> Item
        List<Item> items = itemDocs.stream().map(itemDoc -> BeanUtil.toBean(itemDoc, Item.class)).collect(Collectors.toList());

        // 设置分页结果到 PageBean
        pageBean.setTotal(searchResponse.getHits().getTotalHits().value); // 设置总记录数
        pageBean.setDataset(items); // 设置当前页记录
        pageBean.setTotalpages((int) Math.ceil((double) pageBean.getTotal() / pageBean.getPagesize())); // 计算总页数
        pageBean.setPre(pageBean.getPageno() > 1 ? pageBean.getPageno() - 1 : 1); // 上一页
        pageBean.setNext(pageBean.getPageno() < pageBean.getTotalpages() ? pageBean.getPageno() + 1 : 0); // 下一页

        return pageBean;

    }

    /**
     * 回退库存
     *
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
            item.setStock(item.getStock() + orderDetails.stream().filter(orderDetail -> orderDetail.getItem_id().equals(item.getId())).map(OrderDetail::getNum).collect(Collectors.summingInt(Integer::intValue)));
        }
        // 更新库存
        this.updateBatchById(items);
    }

    @Override
    public List<Item> association(String association) throws IOException {
        // 创建 SearchRequest
        SearchRequest searchRequest = new SearchRequest("shop_items");

        // 创建 BoolQueryBuilder
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", 1)); // 状态为 1

        // 添加模糊查询条件
        boolQuery.should(QueryBuilders.matchQuery("category", association));
        boolQuery.should(QueryBuilders.matchQuery("name", association));
        boolQuery.should(QueryBuilders.matchQuery("brand", association));
        boolQuery.should(QueryBuilders.matchQuery("spec", association));
        boolQuery.should(QueryBuilders.matchQuery("item_details", association));

        // 设置至少需要匹配一个条件
        boolQuery.minimumShouldMatch(1); // 至少需要匹配 1 个条件

        // 创建 SortBuilder
        SortBuilder<?> sortBuilder = SortBuilders.fieldSort("sold").order(SortOrder.ASC);

        // 创建 SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .sort(sortBuilder)
                .from(0) // 偏移量
                .size(6); // 返回的文档数量

        searchRequest.source(searchSourceBuilder);

        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 处理搜索结果
        List<ItemDoc> itemDocs = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            // 反序列化为 Item 对象（根据你的实际需求处理）
            ItemDoc itemDoc = JSON.parseObject(hit.getSourceAsString(), ItemDoc.class);
            itemDocs.add(itemDoc);
        }

        // ItemDoc -> Item
        List<Item> items = itemDocs.stream().map(itemDoc -> BeanUtil.toBean(itemDoc, Item.class)).collect(Collectors.toList());

        return items;

    }
}
