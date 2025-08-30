package com.app.pdf.service;

import com.app.pdf.dao.FileDao;
import com.app.pdf.dao.FileSegmentDao;
import com.app.pdf.entity.FileEntity;
import com.app.pdf.entity.FileOutlineEntity;
import com.app.pdf.entity.FileSegmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    
    @Autowired
    private FileDao fileDao;
    
    @Autowired
    private FileSegmentDao fileSegmentDao;
    
    @Autowired
    private AiClient aiClient;
    
    public Object getOrGenerate(Long fileId) {
        // 查找文件
        FileEntity file = fileDao.getFileById(fileId);
        if (file == null) {
            return null;
        }
        
        // 如果已有摘要，直接返回
        if (file.getSummary() != null && !file.getSummary().isEmpty()) {
            return file.getSummary();
        }
        
        // 生成摘要
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下文档生成摘要:\n\n");
        prompt.append("文档名称: ").append(file.getFilename()).append("\n\n");
        
        // 获取目录信息
        List<FileOutlineEntity> outlines = fileDao.getOutlinesByFileId(fileId);
        if (!outlines.isEmpty()) {
            prompt.append("文档目录:\n");
            for (FileOutlineEntity outline : outlines) {
                for (int i = 0; i < outline.getLevel(); i++) {
                    prompt.append("  ");
                }
                prompt.append(outline.getTitle()).append("\n");
            }
            prompt.append("\n");
        }
        
        // 获取文档内容片段，每页取2段
        prompt.append("文档内容片段:\n");

        // 简化实现：获取前几页的内容片段（每页2段）
        for (int page = 1; page <= 5; page++) {
            List<FileSegmentEntity> segments = fileSegmentDao.getSegmentsByFileIdAndPage(fileId, page, 2);
            if (!segments.isEmpty()) {
                prompt.append("第").append(page).append("页:\n");
                for (FileSegmentEntity segment : segments) {
                    prompt.append(segment.getContent()).append("\n");
                }
                prompt.append("\n");
            }
        }
        
        prompt.append("请根据以上信息生成简洁明了的文档摘要:");
        
        // 调用AI客户端生成摘要
        String summary = aiClient.generateSummary(prompt.toString());
        
        // 保存摘要
        fileDao.updateSummary(fileId, summary);
        
        return summary;
    }
}