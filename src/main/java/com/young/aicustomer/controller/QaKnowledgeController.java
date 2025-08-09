package com.young.aicustomer.controller;

import com.young.aicustomer.dto.Result;
import com.young.aicustomer.dto.query.PageQuery;
import com.young.aicustomer.service.IQaKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class QaKnowledgeController {

    private final IQaKnowledgeService qaKnowledgeService;

    @PostMapping("/update")
    public Result updateKnowledge(@RequestParam("file") MultipartFile file) {
        return qaKnowledgeService.updateKnowledge(file);
    }

    @PostMapping("/page")
    public Result getKnowledgePage(@RequestBody PageQuery query) {
        return qaKnowledgeService.getKnowledgePage(query);
    }

}
