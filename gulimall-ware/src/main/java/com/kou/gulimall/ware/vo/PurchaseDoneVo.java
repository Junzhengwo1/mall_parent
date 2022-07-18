package com.kou.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id; //采购单Id

    private List<PurchaseItemDoneVo> items;
}
