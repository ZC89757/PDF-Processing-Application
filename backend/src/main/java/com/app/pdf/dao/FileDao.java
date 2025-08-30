package com.app.pdf.dao;

import com.app.pdf.entity.FileEntity;
import com.app.pdf.entity.FileOutlineEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileDao {

    @Insert("INSERT INTO t_files(filename,size,status) VALUES(#{filename},#{size},'UPLOADING')")
    @SelectKey(
            statement = "SELECT currval(pg_get_serial_sequence('t_files','id'))",
            keyProperty = "id",
            before = false,
            resultType = Long.class
    )
    void insertFile(FileEntity file);

    @Update("UPDATE t_files SET path=#{path},status='UPLOADED' WHERE id=#{id}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    void updateFilePath(String path,  Long id);

    @Update("UPDATE t_files SET status='PARSING' WHERE id=#{id}")
    void updateStatusToParsing(Long id);

    @Update({
            "<script>",
            "UPDATE t_files",
            "  <set>",
            "    <if test='status != null'>status = #{status},</if>",
            "    has_outline = #{hasOutline}",
            "  </set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void updateStatusAndOutline(@Param("id") Long id,
                                @Param("status") String status,
                                @Param("hasOutline") Boolean hasOutline);

    @Update("UPDATE t_files SET summary=#{summary} WHERE id=#{id}")
    void updateSummary(@Param("id") Long id, @Param("summary") String summary);

    @Select("<script>" +
            "SELECT f.id, f.filename, f.upload_time, f.status, f.has_outline, " +
            "       (f.summary IS NOT NULL) AS summary_exists" +
            "<if test='keyword != null and keyword != \"\"'>, s.page AS page</if> " +
            "FROM t_files f " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  JOIN ( " +
            "    SELECT file_id, page " +
            "    FROM t_file_segments " +
            "    WHERE content ILIKE CONCAT('%', #{keyword}, '%') " +
            "  ) s ON s.file_id = f.id " +
            "</if> " +
            "ORDER BY f.upload_time DESC " +
            "LIMIT #{size} OFFSET (#{page} - 1) * #{size} " +
            "</script>")
    List<Map<String, Object>> listFiles(@Param("page") int page, @Param("size") int size, @Param("keyword") String keyword);
    
    @Select("SELECT * FROM t_files WHERE id = #{id}")
    FileEntity getFileById(@Param("id") Long id);
    
    @Select("SELECT title, page, level FROM t_file_outline WHERE file_id = #{fileId} ORDER BY id")
    List<FileOutlineEntity> getOutlinesByFileId(@Param("fileId") Long fileId);
}