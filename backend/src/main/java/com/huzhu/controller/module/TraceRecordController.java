package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.TraceRecord;
import com.huzhu.service.TraceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trace-records")
public class TraceRecordController {

    @Autowired
    private TraceRecordService traceRecordService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<TraceRecord>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                       @RequestParam(defaultValue = "10") Long pageSize,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String batchNo) {
        Page<TraceRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<TraceRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("trace_code", keyword)
                    .or().like("product_name", keyword)
                    .or().like("product_code", keyword)
                    .or().like("batch_no", keyword)
                    .or().like("stage", keyword));
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.eq("batch_no", batchNo);
        }
        wrapper.orderByDesc("id");
        traceRecordService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/trace-code/{traceCode}")
    public ApiResponse<List<TraceRecord>> queryByTraceCode(@PathVariable String traceCode) {
        QueryWrapper<TraceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("trace_code", traceCode).orderByAsc("id");
        return ApiResponse.success(traceRecordService.list(wrapper));
    }

    @GetMapping("/batch/{batchNo}")
    public ApiResponse<List<TraceRecord>> queryByBatchNo(@PathVariable String batchNo) {
        QueryWrapper<TraceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_no", batchNo).orderByAsc("id");
        return ApiResponse.success(traceRecordService.list(wrapper));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody TraceRecord item) {
        return ApiResponse.success("创建成功", traceRecordService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody TraceRecord item) {
        item.setId(id);
        return ApiResponse.success("更新成功", traceRecordService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", traceRecordService.removeById(id));
    }
}
