package com.app.pdf.controller;

import com.app.pdf.dao.FileDao;
import com.app.pdf.entity.FileOutlineEntity;
import com.app.pdf.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class OutlineController {

    @Autowired
    private FileDao fileDao;

    @GetMapping("/{id}/outline")
    public Result getOutline(@PathVariable Long id) {
        List<FileOutlineEntity> outlines = fileDao.getOutlinesByFileId(id);
        return Result.success(buildTree(outlines));
    }

    private Object buildTree(List<FileOutlineEntity> outlines) {
        return outlines;
    }
}
