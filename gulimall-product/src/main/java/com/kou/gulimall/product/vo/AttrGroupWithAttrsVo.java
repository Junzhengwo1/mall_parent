package com.kou.gulimall.product.vo;

import com.kou.gulimall.product.entity.AttrEntity;
import com.kou.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {


    private List<AttrEntity> attrs;

}
