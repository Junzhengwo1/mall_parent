package com.kou.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.kou.gulimall.search.Bean.UserSelf;
import com.kou.gulimall.search.cofig.GulimallElasticSearchConfig;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;



    @Test
    void contextLoads() {
        //测试链接 elasticsearch
        System.out.println(client);
    }

    /**
     * 测试存储数据到es
     *
     */
    @Test
    void indexTest() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        UserSelf userSelf = new UserSelf();
        userSelf.setName("king");
        userSelf.setAge(23);
        String jsonString = JSON.toJSONString(userSelf);
        indexRequest.source(jsonString, XContentType.JSON); //要保存的内容
        //客户端执行操作
        IndexResponse indexResponse = client.index(indexRequest, GulimallElasticSearchConfig.CMMREQUEST_OPTIONS);
        System.out.println(indexResponse);

        //提取有用的响应数据
    }




    /**
     * 测试检索数据
     *
     */
    @Test
    void searchTest() throws IOException {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("users");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构造检索条件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregations();

        sourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.source(sourceBuilder);//指定检索性条件
        SearchResponse s = client.search(searchRequest, GulimallElasticSearchConfig.CMMREQUEST_OPTIONS);
        //拿到了 检索的response
        //结果分析
        SearchHits hits = s.getHits();
        //System.out.println(hits);
        SearchHit[] hitList = hits.getHits();
        for (SearchHit documentFields : hitList) {
            String sourceAsString = documentFields.getSourceAsString();
            UserSelf userSelf = JSON.parseObject(sourceAsString,UserSelf.class);
            System.out.println(userSelf);
        }
//        System.out.println(s);

    }



}

