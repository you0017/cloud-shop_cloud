package com.yc;

import com.yc.service.ItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class ElasticDocumentTest {
    private RestHighLevelClient client;
    private ItemService itemService;


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
