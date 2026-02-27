package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("customer")
public class Customer implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String businessScope;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}