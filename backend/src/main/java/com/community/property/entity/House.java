package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("house")
public class House extends BaseEntity {

    private Long userId;

    private String buildingNo;

    private String unitNo;

    private String roomNo;

    private BigDecimal area;

    private String type;

    private Integer status;
}