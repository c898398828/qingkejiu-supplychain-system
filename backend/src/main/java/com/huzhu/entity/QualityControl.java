package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("quality_control")
public class QualityControl implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String qualityNo;
    private String productName;
    private String batchNo;
    private String testItem;
    private String testResult;
    private String testStandard;
    private String tester;
    private String testDate;
    private String status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}