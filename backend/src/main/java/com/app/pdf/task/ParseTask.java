package com.app.pdf.task;

import com.app.pdf.service.FileService;
import com.app.pdf.service.ParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ParseTask {

    
    @Autowired
    private ParseService parseService;
    
    @Scheduled(fixedDelay = 1000)
    public void processParseQueue() {
        Long fileId = FileService.getParseQueue().poll();
        if (fileId != null) {
            parseService.parsePdf(fileId);
        }
    }
}