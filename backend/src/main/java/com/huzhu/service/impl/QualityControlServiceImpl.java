package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.QualityControl;
import com.huzhu.mapper.QualityControlMapper;
import com.huzhu.service.QualityControlService;
import org.springframework.stereotype.Service;

@Service
public class QualityControlServiceImpl extends ServiceImpl<QualityControlMapper, QualityControl> implements QualityControlService {
}