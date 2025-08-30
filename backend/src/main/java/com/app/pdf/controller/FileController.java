package com.app.pdf.controller;

import com.app.pdf.entity.FileEntity;
import com.app.pdf.model.Result;
import com.app.pdf.service.FileService;
import com.app.pdf.service.ParseService;
import com.app.pdf.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @GetMapping("/{fileId}/pdf")
    public ResponseEntity<Resource> getPdfFile(@PathVariable Long fileId) {
        FileEntity fileEntity = fileService.getFileById(fileId);
        if (fileEntity == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(fileEntity.getPath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(path.toFile());

            // 中文文件名需要 URL 编码
            String encodedFileName = URLEncoder.encode(fileEntity.getFilename(), StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            // inline 可在浏览器直接打开 PDF
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}