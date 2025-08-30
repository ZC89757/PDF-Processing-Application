package com.app.pdf.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Data
public class FileEntity {
    private Long id;
    private String filename;
    private Long size;
    private String path;
    private String status; // UPLOADED, PARSING, READY, FAILED
    private Boolean hasOutline;
    private OffsetDateTime uploadTime;
    private String summary;
}