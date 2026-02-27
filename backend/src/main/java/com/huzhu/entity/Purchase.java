package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("purchase")
public class Purchase implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String purchaseNo;
    private String supplierName;
    private String materialName;
    private Double quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String purchaseDate;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}