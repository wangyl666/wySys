package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visitor")
public class Visitor extends BaseEntity {

    private Long userId;

    private String visitorName;

    private String visitorPhone;

    private String visitorIdCard;

    private Integer visitorCount;

    private String visitReason;

    private String visitAddress;

    private LocalDateTime visitTime;

    private LocalDateTime leaveTime;

    private String status;

    private Long auditId;

    private String auditName;

    private String auditComment;

    private LocalDateTime auditTime;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String phone;
}