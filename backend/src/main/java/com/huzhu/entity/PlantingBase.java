package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("planting_base")
public class PlantingBase implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String baseName;
    private String location;
    private Double area;
    private String soilType;
    private String climate;
    private String manager;
    private String contactPhone;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}