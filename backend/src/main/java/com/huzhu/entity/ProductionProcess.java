package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("production_process")
public class ProductionProcess implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String processNo;
    private String productName;
    private String batchNo;
    private Double quantity;
    private String unit;
    private String startDate;
    private String endDate;
    private String status;
    private String operator;
    private String remark;
    private Date createTime;
    private Date updateTime;
}