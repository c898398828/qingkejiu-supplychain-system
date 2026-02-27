package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("delivery")
public class Delivery implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deliveryNo;
    private String orderNo;
    private String customerName;
    private String deliveryCompany;
    private String trackingNo;
    private String deliveryDate;
    private String status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
