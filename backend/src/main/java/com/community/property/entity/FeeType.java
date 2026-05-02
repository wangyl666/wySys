package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fee_type")
public class FeeType extends BaseEntity {

    private String typeName;

    private String unit;

    private BigDecimal price;

    private String description;

    private Integer status;
}