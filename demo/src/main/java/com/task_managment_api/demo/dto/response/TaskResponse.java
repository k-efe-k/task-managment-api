package com.task_managment_api.demo.dto.response;

import com.task_managment_api.demo.domain.entity.Task;
import com.task_managment_api.demo.domain.entity.Task.Priority;
import com.task_managment_api.demo.domain.entity.Task.Status;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime dueDate;
    private Integer position;
    private Long assigneeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags;

    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .position(task.getPosition())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .tags(task.getTags() != null ? new HashSet<>(task.getTags()) : new HashSet<>())
                .build();
    }
}
