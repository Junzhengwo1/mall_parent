package com.kou.gulimall.common.exception;

public class NoStockException extends RuntimeException {

    private Long skuId;
    private String msg;

    public  NoStockException(Long skuId){
        super(skuId+"没有足够的库存了");
    }



    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
