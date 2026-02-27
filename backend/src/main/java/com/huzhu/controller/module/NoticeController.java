package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.Notice;
import com.huzhu.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<Notice>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                  @RequestParam(defaultValue = "10") Long pageSize,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("notice_title", keyword)
                    .or().like("notice_content", keyword)
                    .or().like("publisher", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("is_top").orderByDesc("id");
        noticeService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Notice item) {
        return ApiResponse.success("创建成功", noticeService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Notice item) {
        item.setId(id);
        return ApiResponse.success("更新成功", noticeService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", noticeService.removeById(id));
    }
}
