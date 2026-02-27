package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.DocumentFile;
import com.huzhu.service.DocumentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentFileService documentFileService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<DocumentFile>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                        @RequestParam(defaultValue = "10") Long pageSize,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        Page<DocumentFile> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DocumentFile> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("title", keyword)
                    .or().like("file_name", keyword)
                    .or().like("uploader", keyword)
                    .or().like("version", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        documentFileService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody DocumentFile item) {
        return ApiResponse.success("创建成功", documentFileService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody DocumentFile item) {
        item.setId(id);
        return ApiResponse.success("更新成功", documentFileService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", documentFileService.removeById(id));
    }
}
