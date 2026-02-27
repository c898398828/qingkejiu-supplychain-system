package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.TraceRecord;
import com.huzhu.mapper.TraceRecordMapper;
import com.huzhu.service.TraceRecordService;
import org.springframework.stereotype.Service;

@Service
public class TraceRecordServiceImpl extends ServiceImpl<TraceRecordMapper, TraceRecord> implements TraceRecordService {
}
