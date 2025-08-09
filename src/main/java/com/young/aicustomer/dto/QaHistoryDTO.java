package com.young.aicustomer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QaHistoryDTO {
    private String role;
    private String content;
    private LocalDateTime time;
}
