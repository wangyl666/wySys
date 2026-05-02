package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {

    private String title;

    private String content;

    private String type;

    private Long publisherId;

    private String publisherName;

    private Integer isTop;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer viewCount;

    private Integer status;

    @TableField(exist = false)
    private Boolean isRead;
}