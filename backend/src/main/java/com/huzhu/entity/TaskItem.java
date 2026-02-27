package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("task_item")
public class TaskItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskTitle;
    private String taskContent;
    private String assignee;
    private String dueDate;
    private String priority;
    private String status;
    private Date createTime;
    private Date updateTime;
}
