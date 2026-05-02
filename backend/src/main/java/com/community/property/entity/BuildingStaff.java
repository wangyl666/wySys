package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("building_staff")
public class BuildingStaff extends BaseEntity {

    private Long buildingId;

    private String buildingNo;

    private Long staffId;

    private String staffName;

    private LocalDateTime assignTime;
}
