package com.app.pdf.controller;

import com.app.pdf.model.Result;
import com.app.pdf.service.FileService;
import com.app.pdf.service.ParseService;
import com.app.pdf.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private ParseService parseService;
    
    @Autowired
    private SummaryService summaryService;
    
    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.saveUpload(file);
    }
    
    @GetMapping
    public Result listFiles(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(required = false) String keyword) {
        return fileService.listFiles(page, size, keyword);
    }
    
    @PostMapping("/{id}/parse")
    public Result triggerParse(@PathVariable Long id) {
        parseService.enqueueParse(id);
        parseService.parsePdf(id);
        return Result.success("解析任务已启动");
    }
    
    @GetMapping("/{id}/summary")
    public Result getSummary(@PathVariable Long id) {
        Object summary = summaryService.getOrGenerate(id);
        if (summary == null) {
            return Result.error(404, "文件不存在");
        }
        return Result.success(summary);
    }
}