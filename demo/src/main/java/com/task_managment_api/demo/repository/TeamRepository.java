package com.task_managment_api.demo.repository;

import com.task_managment_api.demo.domain.entity.Team;
import com.task_managment_api.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByMembersContaining(User user);
    List<Team> findByCreator(User creator);
}
