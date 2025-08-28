package com.app.pdf.service;

import com.app.pdf.dao.FileDao;
import com.app.pdf.dao.FileOutlineDao;
import com.app.pdf.entity.FileOutlineEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutlineService {
    
    @Autowired
    private FileOutlineDao fileOutlineDao;
    
    @Autowired
    private FileDao fileDao;
    
    public Boolean parseAndSave(Long fileId, PDDocument document) throws IOException {
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
        if (outline == null) {
            fileDao.updateStatusAndOutline(fileId, null, false);
            return false;
        }
        
        List<FileOutlineEntity> outlines = new ArrayList<>();
        parseOutlineItems(outline.children(), 1, fileId, outlines);
        
        if (!outlines.isEmpty()) {
            fileOutlineDao.batchInsertOutlines(outlines);
            fileDao.updateStatusAndOutline(fileId, null, true);
        } else {
            fileDao.updateStatusAndOutline(fileId, null, false);
        }
        
        return !outlines.isEmpty();
    }
    
    private void parseOutlineItems(Iterable<PDOutlineItem> items, int level, Long fileId, List<FileOutlineEntity> outlines) {
        for (PDOutlineItem item : items) {
            FileOutlineEntity outline = new FileOutlineEntity();
            outline.setFileId(fileId);
            outline.setTitle(item.getTitle());
            // 获取页面号
            try {
                if (item.findDestinationPage() != null) {
                    outline.setPage(item.findDestinationPage().getNumber());
                }
            } catch (IOException e) {
                // 页面信息获取失败，设置为0
                outline.setPage(0);
            }
            outline.setLevel(level);
            outlines.add(outline);
            
            // 递归处理子项
            parseOutlineItems(item.children(), level + 1, fileId, outlines);
        }
    }
}