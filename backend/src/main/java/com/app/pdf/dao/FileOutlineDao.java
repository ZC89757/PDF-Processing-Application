package com.app.pdf.dao;

import com.app.pdf.entity.FileOutlineEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileOutlineDao {
    
    @Insert("<script>" +
            "INSERT INTO t_file_outline(file_id, title, page, level) VALUES " +
            "<foreach collection='outlines' item='outline' separator=','>" +
            "(#{outline.fileId}, #{outline.title}, #{outline.page}, #{outline.level})" +
            "</foreach>" +
            "</script>")
    void batchInsertOutlines(List<FileOutlineEntity> outlines);
}