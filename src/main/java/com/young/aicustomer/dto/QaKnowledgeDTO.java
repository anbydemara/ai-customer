package com.young.aicustomer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QaKnowledgeDTO {
    private String question;

    private String answer;
}
