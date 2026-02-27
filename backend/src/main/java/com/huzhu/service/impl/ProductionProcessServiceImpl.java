package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.ProductionProcess;
import com.huzhu.mapper.ProductionProcessMapper;
import com.huzhu.service.ProductionProcessService;
import org.springframework.stereotype.Service;

@Service
public class ProductionProcessServiceImpl extends ServiceImpl<ProductionProcessMapper, ProductionProcess> implements ProductionProcessService {
}