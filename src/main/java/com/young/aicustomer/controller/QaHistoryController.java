package com.young.aicustomer.controller;

import com.young.aicustomer.dto.Result;
import com.young.aicustomer.service.IQaHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class QaHistoryController {

    private final IQaHistoryService qaHistoryService;

    @PostMapping("/page/{limit}")
    public Result getHistory(@PathVariable("limit") int limit) { // @RequestParam(defaultValue = "10")
        return qaHistoryService.getHistory(limit);
    }

    @GetMapping("/{sessionId}")
    public Result getSessionHistory(@PathVariable("sessionId") String sessionId) {
        return qaHistoryService.getSessionHistory(sessionId);
    }

    @GetMapping("/sessions")
    public Result getSessionIds() {
        return qaHistoryService.getSessionIds();
    }

    @DeleteMapping("/delete/{sessionId}")
    public Result deleteSession(@PathVariable("sessionId") String sessionId) {
        return qaHistoryService.deleteSession(sessionId);
    }
}
