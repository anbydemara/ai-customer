package com.young.aicustomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.dto.query.PageQuery;
import com.young.aicustomer.entity.QaKnowledge;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface IQaKnowledgeService extends IService<QaKnowledge> {
    Optional<String> findKnowledge(String question);

    Result updateKnowledge(MultipartFile file);

    Result getKnowledgePage(PageQuery pageQuery);
}
