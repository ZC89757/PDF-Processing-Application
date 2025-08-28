package com.app.pdf.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileEntity {
    private Long id;
    private String filename;
    private Long size;
    private String path;
    private String status; // UPLOADED, PARSING, READY, FAILED
    private Boolean hasOutline;
    private LocalDateTime uploadTime;
    private String summary;
}