package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("trace_record")
public class TraceRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceCode;
    private String productName;
    private String productCode;
    private String batchNo;
    private String stage;
    private String stageDetail;
    private String operator;
    private String recordTime;
    private Date createTime;
    private Date updateTime;
}
