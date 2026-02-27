package com.huzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("document_file")
public class DocumentFile implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String fileName;
    private String fileUrl;
    private String uploader;
    private String version;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
