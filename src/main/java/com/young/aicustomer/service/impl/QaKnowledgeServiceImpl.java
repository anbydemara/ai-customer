package com.young.aicustomer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.young.aicustomer.dto.ErrorCode;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.dto.query.PageDTO;
import com.young.aicustomer.dto.query.PageQuery;
import com.young.aicustomer.entity.QaKnowledge;
import com.young.aicustomer.mapper.QaKnowledgeMapper;
import com.young.aicustomer.service.IQaHistoryService;
import com.young.aicustomer.service.IQaKnowledgeService;
import com.young.aicustomer.vo.KnowledgeVo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class QaKnowledgeServiceImpl extends ServiceImpl<QaKnowledgeMapper, QaKnowledge> implements IQaKnowledgeService {

//    private final IQaHistoryService qaHistoryService;
    private IQaHistoryService qaHistoryService;

    @Autowired
    public void setQaHistoryService(IQaHistoryService qaHistoryService) {
        this.qaHistoryService = qaHistoryService;
    }

    @Override
    public Optional<String> findKnowledge(String question) {
        QaKnowledge knowledge = this.lambdaQuery().eq(QaKnowledge::getQuestion, question).one();
        if (knowledge == null) {
            return Optional.empty();
        } else {
            return Optional.of(knowledge.getAnswer());
        }
    }

    @Transactional
    @Override
    public Result updateKnowledge(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) return Result.fail(ErrorCode.FILE_SIZE_OVERFLOW.getCode(), ErrorCode.FILE_SIZE_OVERFLOW.getMsg());
        String filename = file.getOriginalFilename();
        List<QaKnowledge> knowledgeList;
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                knowledgeList = parseExcel(file.getInputStream());
            } else if (filename.endsWith(".csv")) {
                knowledgeList = parseCsv(file.getInputStream());
            } else if (filename.endsWith(".json")) {
                knowledgeList = parseJson(file.getInputStream());
            } else {
                return Result.fail(ErrorCode.FILE_TYPE_ERROR.getCode(), ErrorCode.FILE_TYPE_ERROR.getMsg());
            }
            this.remove(new QueryWrapper<>());
            this.saveBatch(knowledgeList);
            return Result.success();

        } catch (IOException e) {
            return Result.fail(ErrorCode.FILE_UPLOAD_ERROR.getCode(), ErrorCode.FILE_UPLOAD_ERROR.getMsg());
        }
    }

    @Override
    public Result getKnowledgePage(PageQuery query) {
        // 1. 构建分页条件
        Page<QaKnowledge> page = query.toMpPageDefault();
        // 2. 分页查询
        Page<QaKnowledge> resultPage = this.lambdaQuery().page(page);

//        Page<QaKnowledge> resultPage = this.lambdaQuery().select().page(page);
        return Result.success(
                PageDTO.of(page, knowledge -> {
                    return BeanUtil.copyProperties(knowledge, KnowledgeVo.class);
                })
        );
    }

    // 文件解析
    public List<QaKnowledge> parseExcel(InputStream is) throws IOException {
        List<QaKnowledge> result = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // 跳过表头
            String q = row.getCell(0).getStringCellValue();
            String a = row.getCell(1).getStringCellValue();
            if (!StringUtils.hasText(q) || !StringUtils.hasText(a)) continue;
            result.add(new QaKnowledge(null, q.trim(), a.trim(), null, null));
        }
        return result;
    }

    public List<QaKnowledge> parseCsv(InputStream is) throws IOException {
        List<QaKnowledge> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean skip = true;
        while ((line = reader.readLine()) != null) {
            if (skip) { skip = false; continue; } // 跳表头
            String[] parts = line.split(",", 2);
            if (parts.length < 2) continue;
            String q = parts[0].trim(), a = parts[1].trim();
            if (!q.isEmpty() && !a.isEmpty()) {
                result.add(new QaKnowledge(null, q, a, null, null));
            }
        }
        return result;
    }

    public List<QaKnowledge> parseJson(InputStream is) throws IOException {
        try (InputStream inputStream = is) {
            String json = readInputStream(inputStream); // JDK 1.8兼容写法
            JSONArray array = JSON.parseArray(json);

            List<QaKnowledge> result = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String q = obj.getString("question");
                String a = obj.getString("answer");
                if (StringUtils.hasText(q) && StringUtils.hasText(a)) {
                    result.add(new QaKnowledge(null, q.trim(), a.trim(), null, null));
                }
            }
            return result;
        }
    }

    public static String readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString("UTF-8");
    }
}
