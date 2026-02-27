package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("notice")
public class Notice implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String noticeTitle;
    private String noticeContent;
    private String publisher;
    private Integer isTop;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
