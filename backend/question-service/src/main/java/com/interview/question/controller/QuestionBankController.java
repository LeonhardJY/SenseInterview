package com.interview.question.controller;

import com.interview.common.result.Result;
import com.interview.question.entity.QuestionBank;
import com.interview.question.service.QuestionBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "题库管理", description = "题库相关接口")
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @Operation(summary = "获取题目列表")
    @GetMapping("/list")
    public Result<List<QuestionBank>> list() {
        List<QuestionBank> list = questionBankService.list();
        return Result.success(list);
    }

    @Operation(summary = "根据分类获取题目")
    @GetMapping("/category/{category}")
    public Result<List<QuestionBank>> listByCategory(@PathVariable String category) {
        List<QuestionBank> list = questionBankService.findByCategory(category);
        return Result.success(list);
    }

    @Operation(summary = "根据难度获取题目")
    @GetMapping("/level/{level}")
    public Result<List<QuestionBank>> listByLevel(@PathVariable String level) {
        List<QuestionBank> list = questionBankService.findByLevel(level);
        return Result.success(list);
    }

    @Operation(summary = "新增题目")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody QuestionBank questionBank) {
        questionBankService.save(questionBank);
        return Result.success();
    }

    @Operation(summary = "更新题目")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody QuestionBank questionBank) {
        questionBankService.updateById(questionBank);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionBankService.removeById(id);
        return Result.success();
    }
}