package com.task_managment_api.demo.controller;

import com.task_managment_api.demo.domain.entity.Task;
import com.task_managment_api.demo.service.AiTaskDescriptionService;
import com.task_managment_api.demo.service.AiTaskDescriptionService.AiAnalysisResult;
import com.task_managment_api.demo.service.AiTaskDescriptionService.AiReportDto;
import com.task_managment_api.demo.service.AiTaskDescriptionService.GeneratedTaskDto;
import com.task_managment_api.demo.dto.request.AiRequest;
import com.task_managment_api.demo.dto.request.PromptRequest;
import com.task_managment_api.demo.dto.response.TaskResponse;
import com.task_managment_api.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiTaskDescriptionService aiTaskDescriptionService;
    private final TaskService taskService;

    @PostMapping("/generate-description")
    public ResponseEntity<AiAnalysisResult> generateDescription(@RequestBody AiRequest request) {
        AiAnalysisResult result = aiTaskDescriptionService.analyzeAndGenerate(request.getTitle(), request.getPriority());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-tasks")
    public ResponseEntity<List<GeneratedTaskDto>> generateTasksFromPrompt(@RequestBody PromptRequest request) {
        List<GeneratedTaskDto> generatedTasks = aiTaskDescriptionService.generateTasksFromPrompt(request.getPrompt());
        return ResponseEntity.ok(generatedTasks);
    }

    @GetMapping("/generate-report")
    public ResponseEntity<AiReportDto> generateReport() {
        List<TaskResponse> userTasks = taskService.getAllTasks();
        AiReportDto report = aiTaskDescriptionService.generateExecutiveReport(userTasks);
        return ResponseEntity.ok(report);
    }
}
