package com.young.aicustomer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionDTO {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String content;
}
