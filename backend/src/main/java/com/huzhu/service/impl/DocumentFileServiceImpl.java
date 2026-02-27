package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.DocumentFile;
import com.huzhu.mapper.DocumentFileMapper;
import com.huzhu.service.DocumentFileService;
import org.springframework.stereotype.Service;

@Service
public class DocumentFileServiceImpl extends ServiceImpl<DocumentFileMapper, DocumentFile> implements DocumentFileService {
}
