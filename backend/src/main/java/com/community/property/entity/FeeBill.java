package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fee_bill")
public class FeeBill extends BaseEntity {

    private Long userId;

    private Long houseId;

    private Long feeTypeId;

    private String feeTypeName;

    private String billNo;

    private String billMonth;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private BigDecimal lateFee;

    private BigDecimal totalAmount;

    private LocalDate payableDate;

    private String status;

    private BigDecimal paidAmount;

    private LocalDateTime paidTime;

    private String paymentMethod;

    private String transactionNo;

    private String remark;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String phone;

    @TableField(exist = false)
    private String buildingNo;

    @TableField(exist = false)
    private String unitNo;

    @TableField(exist = false)
    private String roomNo;
}