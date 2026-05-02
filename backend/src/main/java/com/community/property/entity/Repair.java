package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair")
public class Repair extends BaseEntity {

    private Long userId;

    private String title;

    private String content;

    private String type;

    private String images;

    private String contactName;

    private String contactPhone;

    private String address;

    private LocalDateTime preferredTime;

    private String status;

    private Long staffId;

    private String staffName;

    private String staffPhone;

    private String processContent;

    private LocalDateTime processTime;

    private Integer rating;

    private String comment;

    private LocalDateTime commentTime;

    @TableField(exist = false)
    private List<String> imageList;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String phone;
}