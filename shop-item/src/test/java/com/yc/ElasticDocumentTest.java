package com.yc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.bean.Item;
import com.yc.bean.ItemDoc;
import com.yc.mapper.ItemMapper;
import com.yc.service.ItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;


//@SpringBootTest(classes = ShopItemApplication.class)
public class ElasticDocumentTest {
    private RestHighLevelClient client;
    //@Autowired
    private ItemService itemService;
    //@Autowired
    private ItemMapper itemMapper;


    @Test
    void testIndexDoc() throws IOException {
        //1.准备Request
        IndexRequest request = new IndexRequest("items").id("1");

        //2.准备请求参数
        request.source("{}", XContentType.JSON);

        //3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulkIndexDoc() throws IOException {
        int pageNo = 1;
        int pageSize = 6;

        while (true){
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()){
                return;
            }

            //1.准备Request
            BulkRequest request = new BulkRequest();
            for (Item item : records) {
                //2.准备请求参数
                request.add(new IndexRequest("shop_items")
                        .id(item.getId())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)),XContentType.JSON));
            }
            //发送请求
            client.bulk(request,RequestOptions.DEFAULT);
            //翻页
            pageNo++;
        }


    }


    @Test
    void testConnection() {
        System.out.println(client);
    }

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client!=null){
            client.close();
        }
    }

    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": { \"type\": \"keyword\" },\n" +
            "      \"name\": { \"type\": \"text\", \"analyzer\": \"ik_smart\" },\n" +
            "      \"price\": { \"type\": \"integer\" },\n" +
            "      \"image\": { \"type\": \"keyword\", \"index\": false },\n" +
            "      \"category\": { \"type\": \"keyword\" },\n" +
            "      \"brand\": { \"type\": \"keyword\" },\n" +
            "      \"sold\": { \"type\": \"keyword\" },\n" +
            "      \"commentCount\": { \"type\": \"integer\", \"index\": false },\n" +
            "      \"isAD\": { \"type\": \"boolean\" },\n" +
            "      \"updateTime\": { \"type\": \"date\" }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
