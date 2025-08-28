package com.app.pdf.entity;

import lombok.Data;

@Data
public class FileOutlineEntity {
    private Long id;
    private Long fileId;
    private String title;
    private Integer page;
    private Integer level;
}