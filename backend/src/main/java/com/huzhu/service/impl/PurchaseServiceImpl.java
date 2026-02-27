package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.Purchase;
import com.huzhu.mapper.PurchaseMapper;
import com.huzhu.service.PurchaseService;
import org.springframework.stereotype.Service;

@Service
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Purchase> implements PurchaseService {
}