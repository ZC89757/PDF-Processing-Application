package com.app.pdf.service;

import com.app.pdf.dao.FileDao;
import com.app.pdf.entity.FileEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

@Service
public class ParseService {
    
    @Autowired
    private FileDao fileDao;
    
    @Autowired
    private OutlineService outlineService;
    
    @Autowired
    private SegmentService segmentService;
    
    public void enqueueParse(Long fileId) {
        FileService.getParseQueue().offer(fileId);
    }
    
    @Async
    public void parsePdf(Long fileId) {
        // 更新状态为解析中
        fileDao.updateStatusToParsing(fileId);
        
        try {
            // 获取文件信息
            FileEntity fileEntity = fileDao.getFileById(fileId);
            if (fileEntity == null) {
                throw new IllegalArgumentException("文件不存在");
            }
            
            // 打开PDF文档
            File file = new File(fileEntity.getPath());
            try (PDDocument document = PDDocument.load(file)) {
                // 解析目录
                Boolean hasOutline = outlineService.parseAndSave(fileId, document);
                
                // 分段并保存
                segmentService.segmentAndPersist(fileId, document);
                
                // 更新状态为完成
                fileDao.updateStatusAndOutline(fileId, "READY", hasOutline);
            }
        } catch (Exception e) {
            // 更新状态为失败
            fileDao.updateStatusAndOutline(fileId, "FAILED", false);
            e.printStackTrace(); // 实际项目中应该使用日志记录
        }
    }
}