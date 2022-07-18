package com.kou.gulimall.search.service;


import com.kou.gulimall.search.vo.SearchParam;
import com.kou.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    SearchResult search(SearchParam param);
}
