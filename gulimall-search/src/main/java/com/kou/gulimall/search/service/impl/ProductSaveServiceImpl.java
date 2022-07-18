package com.kou.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.kou.gulimall.common.to.SkuEsModel;
import com.kou.gulimall.search.cofig.GulimallElasticSearchConfig;
import com.kou.gulimall.search.constant.EsConstant;
import com.kou.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        //1、给es建立索引 并建立映射关系

        //2、保存数据到es
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(skuEsModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            //内容Source
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });

        BulkResponse bulk = client.bulk(bulkRequest, GulimallElasticSearchConfig.CMMREQUEST_OPTIONS);
        //TODO 如果错误；我们可以处理一下错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成：{}，返回信息",collect,bulk.toString());

        return b;
    }



}
