package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.community.property.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    private Long billId;

    private Long userId;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionNo;

    private String status;

    private LocalDateTime paymentTime;

    private String remark;
}