package com.app.pdf.service;

import com.app.pdf.dao.FileDao;
import com.app.pdf.entity.FileEntity;
import com.app.pdf.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class FileService {
    
    @Autowired
    private FileDao fileDao;
    
    @Value("${storage.root:/data/files}")
    private String storageRoot;
    
    private static final BlockingQueue<Long> parseQueue = new LinkedBlockingQueue<>();
    
    public Result saveUpload(MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        
        if (!"application/pdf".equals(file.getContentType())) {
            return Result.error(400, "只支持PDF文件");
        }
        
        if (file.getSize() > 200 * 1024 * 1024) { // 200MB
            return Result.error(400, "文件大小不能超过200MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            return Result.error(400, "文件名包含非法字符");
        }
        
        try {
            // 创建存储目录
            Path rootPath = Paths.get(storageRoot);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
            
            // 保存文件
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFilename(filename);
            fileEntity.setSize(file.getSize());
            
            // 插入数据库获取ID
            Long fileId = fileDao.insertFile(fileEntity);
            fileEntity.setId(fileId);
            
            // 保存文件到磁盘
            String filePath = storageRoot + "/" + fileId + ".pdf";
            fileEntity.setPath(filePath);
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());
            
            // 添加到解析队列
            parseQueue.offer(fileId);
            
            return Result.success(fileEntity);
        } catch (IOException e) {
            return Result.error(500, "文件保存失败: " + e.getMessage());
        }
    }
    
    public Result listFiles(int page, int size, String keyword) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;
        
        List<FileEntity> files = fileDao.listFiles(page, size, keyword);
        return Result.success(files);
    }
    
    public static BlockingQueue<Long> getParseQueue() {
        return parseQueue;
    }
}