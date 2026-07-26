package com.interview.user.controller;

import com.interview.common.result.Result;
import com.interview.user.entity.Resume;
import com.interview.user.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "简历管理", description = "简历相关接口")
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "获取用户简历列表")
    @GetMapping("/list/{userId}")
    public Result<List<Resume>> getResumeList(@PathVariable("userId") Long userId) {
        List<Resume> list = resumeService.findByUserId(userId);
        return Result.success(list);
    }

    @Operation(summary = "获取简历详情")
    @GetMapping("/{id}")
    public Result<Resume> getResumeById(@PathVariable("id") Long id) {
        Resume resume = resumeService.getById(id);
        return Result.success(resume);
    }

    @Operation(summary = "创建简历")
    @PostMapping("/create")
    public Result<Resume> createResume(@RequestBody Resume resume) {
        Resume created = resumeService.createResume(resume);
        return Result.success(created);
    }

    @Operation(summary = "更新简历")
    @PutMapping("/update")
    public Result<Resume> updateResume(@RequestBody Resume resume) {
        Resume updated = resumeService.updateResume(resume);
        return Result.success(updated);
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(@PathVariable("id") Long id) {
        resumeService.deleteResume(id);
        return Result.success();
    }
}