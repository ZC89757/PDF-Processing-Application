package com.app.pdf.dao;

import com.app.pdf.entity.FileSegmentEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileSegmentDao {
    
    @Insert("<script>" +
            "INSERT INTO t_file_segments(file_id, page, content) VALUES " +
            "<foreach collection='segments' item='segment' separator=','>" +
            "(#{segment.fileId}, #{segment.page}, #{segment.content})" +
            "</foreach>" +
            "</script>")
    void batchInsertSegments(List<FileSegmentEntity> segments);
    
    @Select("SELECT * FROM t_file_segments WHERE file_id = #{fileId} AND page = #{page} LIMIT #{limit}")
    List<FileSegmentEntity> getSegmentsByFileIdAndPage(Long fileId, int page, int limit);
}