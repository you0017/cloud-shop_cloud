package com.yc;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElasticIndexTest {
    private RestHighLevelClient client;

    @Test
    void testConnection() {
        System.out.println(client);
    }

    @Test
    void testCreateIndex() throws IOException {
        //1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("shop_items");
        System.out.println(MAPPING_SHOP_ITEMS);

        //2.准备请求参数
        request.source(MAPPING_SHOP_ITEMS, XContentType.JSON);

        //3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetIndex() throws IOException {
        //1.创建Request对象
        GetIndexRequest request = new GetIndexRequest("shop_items");

        //3.发送请求
        GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
        System.out.println(getIndexResponse.getIndices());
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
            "      \"sold\": { \"type\": \"integer\" },\n" +
            "      \"commentCount\": { \"type\": \"integer\", \"index\": false },\n" +
            "      \"isAD\": { \"type\": \"boolean\" },\n" +
            "      \"updateTime\": { \"type\": \"date\" }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    private static final String MAPPING_SHOP_ITEMS = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"price\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\": {\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"spec\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"sold\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\": {\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"rating\": {\n" +
            "        \"type\": \"binary\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"status\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
}
