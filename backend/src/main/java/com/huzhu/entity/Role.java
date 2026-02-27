package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("role")
public class Role implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String description;
    private Date createTime;
    private Date updateTime;
}