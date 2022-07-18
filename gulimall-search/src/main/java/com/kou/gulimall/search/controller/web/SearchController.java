package com.kou.gulimall.search.controller.web;

import com.kou.gulimall.search.service.MallSearchService;
import com.kou.gulimall.search.vo.SearchParam;
import com.kou.gulimall.search.vo.SearchResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Api(value = "商城检索主页",tags = "检索主页")
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @ApiOperation("检索主页")
    @GetMapping("/list.html")
    public String indexSearch(SearchParam param, Model model){

        SearchResult result =mallSearchService.search(param);
        model.addAttribute(result);

        return "search";
    }


}
