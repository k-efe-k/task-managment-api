package com.task_managment_api.demo.dto.request;

import com.task_managment_api.demo.domain.entity.Task.Priority;
import com.task_managment_api.demo.domain.entity.Task.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private LocalDateTime dueDate;

    private Integer position;

    private Long taskListId;

    private Set<String> tags;
}
