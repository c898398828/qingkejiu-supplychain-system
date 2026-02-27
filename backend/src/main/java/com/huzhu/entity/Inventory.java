package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("inventory")
public class Inventory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String productName;
    private String productCode;
    private String category;
    private Double quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal totalValue;
    private String warehouse;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}