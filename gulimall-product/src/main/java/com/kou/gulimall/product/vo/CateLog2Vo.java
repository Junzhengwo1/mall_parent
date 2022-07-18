package com.kou.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CateLog2Vo {

    private String catalog1Id; //一级父ID

    private List<Catelog3Vo> catalog3List;  //三级子分类

    private String id;

    private String name;




    /**
     * 三级分类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3Vo{

        private String catalog2Id;
        private String id;
        private String name;


    }

}
