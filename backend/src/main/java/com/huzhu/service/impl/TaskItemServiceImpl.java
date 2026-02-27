package com.huzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huzhu.entity.TaskItem;
import com.huzhu.mapper.TaskItemMapper;
import com.huzhu.service.TaskItemService;
import org.springframework.stereotype.Service;

@Service
public class TaskItemServiceImpl extends ServiceImpl<TaskItemMapper, TaskItem> implements TaskItemService {
}
