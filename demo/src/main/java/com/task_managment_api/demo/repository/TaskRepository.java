package com.task_managment_api.demo.repository;

import com.task_managment_api.demo.domain.entity.Task;
import com.task_managment_api.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignee(User assignee);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByTeamId(Long teamId);
    List<Task> findByTeamNullAndAssignee(User assignee);
}
