package com.app.pdf.dao;

import com.app.pdf.entity.FileEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileDao {
    
    @Insert("INSERT INTO t_files(filename,size,path,status) VALUES(#{filename},#{size},#{path},'UPLOADED') RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    Long insertFile(FileEntity file);
    
    @Update("UPDATE t_files SET status='PARSING' WHERE id=#{id}")
    void updateStatusToParsing(Long id);
    
    @Update("UPDATE t_files SET status=#{status}, has_outline=#{hasOutline} WHERE id=#{id}")
    void updateStatusAndOutline(@Param("id") Long id, @Param("status") String status, @Param("hasOutline") Boolean hasOutline);
    
    @Update("UPDATE t_files SET summary=#{summary} WHERE id=#{id}")
    void updateSummary(@Param("id") Long id, @Param("summary") String summary);
    
    @Select("<script>" +
            "SELECT f.id, f.filename, f.upload_time, f.status, f.has_outline, " +
            "(f.summary IS NOT NULL) AS summary_exists " +
            "FROM t_files f " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND EXISTS ( " +
            "   SELECT 1 FROM t_file_segments s " +
            "   WHERE s.file_id = f.id " +
            "   AND s.content ILIKE CONCAT('%', #{keyword}, '%') " +
            ") " +
            "</if> " +
            "ORDER BY f.upload_time DESC " +
            "LIMIT #{size} OFFSET (#{page} - 1) * #{size} " +
            "</script>")
    List<FileEntity> listFiles(@Param("page") int page, @Param("size") int size, @Param("keyword") String keyword);
    
    @Select("SELECT * FROM t_files WHERE id = #{id}")
    FileEntity getFileById(Long id);
    
    @Select("SELECT title, page, level FROM t_file_outline WHERE file_id = #{fileId} ORDER BY id")
    List<com.app.pdf.entity.FileOutlineEntity> getOutlinesByFileId(Long fileId);
}