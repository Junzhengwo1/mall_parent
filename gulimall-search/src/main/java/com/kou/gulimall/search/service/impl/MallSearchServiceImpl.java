package com.kou.gulimall.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.kou.gulimall.search.cofig.GulimallElasticSearchConfig;
import com.kou.gulimall.search.constant.EsConstant;
import com.kou.gulimall.search.service.MallSearchService;
import com.kou.gulimall.search.vo.SearchParam;
import com.kou.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * @param param 检索的所有检索参数
     * @return 返回的结果
     */
    @Override
    public SearchResult search(SearchParam param) {
        //todo 操作Es检索核心代码
        //1、动态构建拆查询语句
        //准备检索请求
        SearchResult result = new SearchResult();
        SearchRequest searchRequest = this.buildSearchRequest(param);

        try {
            //执行检索请求
            SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.CMMREQUEST_OPTIONS);

            //分析响应数据并封装成对应结果
            result = this.buildSearchResult();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 构建检索请求
     * # 模糊匹配，过滤，（按照属性，分类，品牌，价格区间，库存），排序，分页，聚合……
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        //builder 用于构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1、1
        if(StrUtil.isNotEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //1、2
        if(ObjectUtil.isNotNull(param.getCatalog3Id())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        if(CollectionUtil.isNotEmpty(param.getBrandIds())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandIds()));
        }

        if(CollectionUtil.isNotEmpty(param.getAttrs())){
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedQueryBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId=s[0];
                String[] attrVals = s[1].split(":");
                nestedQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue",attrVals));
                //每一个都得生成一个nested
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }

        }

        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));

        if(StrUtil.isNotEmpty(param.getSkuPrice())){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            //todo  价格区间情况处理（值得学习）
            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2){
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            }else if (s.length == 1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQueryBuilder.lte(s[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQueryBuilder.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        sourceBuilder.query(boolQueryBuilder);

        /**
         * 排序，分页，高亮
         */
        if(StrUtil.isNotEmpty(param.getSort())){
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0], sortOrder);
        }

        //todo 计算页码
//        (param.getPageNum()-1)*EsConstant.PRODUCT_PAGE_SIZE
        sourceBuilder.from(1);
        sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        if(StrUtil.isNotEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        log.info("检索条件DSL:Builder{}",sourceBuilder.toString());

        System.out.println(sourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * 组装检索结果
     *
     */
    private SearchResult buildSearchResult() {


        return null;
    }


}
