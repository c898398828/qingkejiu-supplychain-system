package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("`order`")
public class Order implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String address;
    private BigDecimal totalAmount;
    private String orderDate;
    private String status;
    private String paymentMethod;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
