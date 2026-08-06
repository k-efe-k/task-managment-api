package com.task_managment_api.demo.service;

import com.task_managment_api.demo.domain.entity.Task;
import com.task_managment_api.demo.domain.entity.User;
import com.task_managment_api.demo.domain.entity.Team;
import com.task_managment_api.demo.dto.request.TaskCreateRequest;
import com.task_managment_api.demo.dto.request.TaskUpdateRequest;
import com.task_managment_api.demo.dto.response.TaskResponse;
import com.task_managment_api.demo.repository.TaskRepository;
import com.task_managment_api.demo.repository.UserRepository;
import com.task_managment_api.demo.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AiTaskDescriptionService aiTaskDescriptionService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public List<TaskResponse> getTasks(Long teamId) {
        User currentUser = getCurrentUser();
        if (teamId == null) {
            return taskRepository.findByTeamNullAndAssignee(currentUser).stream()
                    .map(TaskResponse::fromEntity)
                    .toList();
        } else {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            if (!team.getMembers().contains(currentUser) && !team.getCreator().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied: You are not a member of this team");
            }
            return taskRepository.findByTeamId(teamId).stream()
                    .map(TaskResponse::fromEntity)
                    .toList();
        }
    }

    public List<TaskResponse> getAllTasks() {
        return getTasks(null);
    }

    public Task getTaskEntityById(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (task.getTeam() != null) {
            Team team = task.getTeam();
            if (!team.getMembers().contains(currentUser) && !team.getCreator().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied: You are not a member of the team this task belongs to.");
            }
        } else {
            if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied: You cannot view tasks belonging to another user.");
            }
        }
        return task;
    }

    public TaskResponse getTaskById(Long id) {
        return TaskResponse.fromEntity(getTaskEntityById(id));
    }

    public TaskResponse createTask(TaskCreateRequest request) {
        User currentUser = getCurrentUser();
        
        Team team = null;
        User assignee = currentUser;

        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            if (!team.getMembers().contains(currentUser) && !team.getCreator().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied: You are not a member of this team");
            }
            if (request.getAssigneeId() != null) {
                User requestedAssignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new RuntimeException("Assignee user not found"));
                if (!team.getMembers().contains(requestedAssignee) && !team.getCreator().getId().equals(requestedAssignee.getId())) {
                    throw new RuntimeException("Assignee is not a member of this team");
                }
                assignee = requestedAssignee;
            }
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : Task.Status.TODO)
                .dueDate(request.getDueDate())
                .position(request.getPosition())
                .assignee(assignee)
                .team(team)
                .creator(currentUser)
                .tags(request.getTags() != null ? request.getTags() : new HashSet<>())
                .build();

        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            String pStr = task.getPriority().name();
            task.setDescription(aiTaskDescriptionService.generateDescription(task.getTitle(), pStr));
        }

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = getTaskEntityById(id);
        
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }
        
        if (request.getAssigneeId() != null) {
            User requestedAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee user not found"));
            if (task.getTeam() != null) {
                Team team = task.getTeam();
                if (!team.getMembers().contains(requestedAssignee) && !team.getCreator().getId().equals(requestedAssignee.getId())) {
                    throw new RuntimeException("Assignee is not a member of the team this task belongs to.");
                }
            }
            task.setAssignee(requestedAssignee);
        }

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = getTaskEntityById(id);
        taskRepository.delete(task);
    }
}
