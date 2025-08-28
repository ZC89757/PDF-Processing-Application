package com.app.pdf.service;

import com.app.pdf.dao.FileSegmentDao;
import com.app.pdf.entity.FileSegmentEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SegmentService {
    
    @Autowired
    private FileSegmentDao fileSegmentDao;
    
    public void segmentAndPersist(Long fileId, PDDocument document) throws IOException {
        int pageCount = document.getNumberOfPages();
        List<FileSegmentEntity> segments = new ArrayList<>();
        
        PDFTextStripper stripper = new PDFTextStripper();
        
        for (int page = 1; page <= pageCount; page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            String text = stripper.getText(document);
            
            // 按段落分隔符分割文本
            String[] paragraphs = text.split("\\n\\s*\\n");
            
            for (String paragraph : paragraphs) {
                String trimmed = paragraph.trim();
                if (!trimmed.isEmpty()) {
                    FileSegmentEntity segment = new FileSegmentEntity();
                    segment.setFileId(fileId);
                    segment.setPage(page);
                    segment.setContent(trimmed);
                    segments.add(segment);
                }
            }
        }
        
        // 批量插入
        if (!segments.isEmpty()) {
            fileSegmentDao.batchInsertSegments(segments);
        }
    }
}