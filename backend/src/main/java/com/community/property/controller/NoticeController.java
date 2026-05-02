package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.Notice;
import com.community.property.entity.User;
import com.community.property.service.NoticeService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "公告通知接口")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;

    @ApiOperation("分页查询已发布的公告（居民端）")
    @GetMapping("/page/published")
    public Result<Page<Notice>> pagePublished(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        Page<Notice> page = noticeService.pagePublished(current, size, type);
        return Result.success(page);
    }

    @ApiOperation("获取公告详情")
    @GetMapping("/{id}")
    public Result<Notice> getDetail(@PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        Notice notice = noticeService.getDetail(id, userId);
        return Result.success(notice);
    }

    @ApiOperation("分页查询公告列表（物业端）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<Notice>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        Page<Notice> page = noticeService.pageByCondition(current, size, title, type, status);
        return Result.success(page);
    }

    @ApiOperation("发布公告")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> publish(@RequestBody Notice notice) {
        User currentUser = userService.getCurrentUser();
        return Result.success(noticeService.publishNotice(notice, currentUser.getId(), currentUser.getRealName()));
    }

    @ApiOperation("更新公告")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> update(@RequestBody Notice notice) {
        return Result.success(noticeService.updateById(notice));
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(noticeService.removeById(id));
    }
}