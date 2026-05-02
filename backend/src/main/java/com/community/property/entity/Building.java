package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("building")
public class Building extends BaseEntity {

    private String buildingNo;

    private String buildingName;

    private Integer totalFloors;

    private Integer unitsPerFloor;

    private Integer totalHouses;

    private String description;

    private Integer status;

    @TableField(exist = false)
    private List<User> staffList;

    @TableField(exist = false)
    private Long[] staffIds;
}
