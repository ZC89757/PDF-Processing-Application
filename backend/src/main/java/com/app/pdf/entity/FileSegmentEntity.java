package com.app.pdf.entity;

import lombok.Data;

@Data
public class FileSegmentEntity {
    private Long id;
    private Long fileId;
    private Integer page;
    private String content;
}