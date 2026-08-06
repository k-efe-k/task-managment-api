package com.task_managment_api.demo.service;

import com.task_managment_api.demo.domain.entity.Team;
import com.task_managment_api.demo.domain.entity.User;
import com.task_managment_api.demo.dto.request.TeamCreateRequest;
import com.task_managment_api.demo.dto.response.TeamResponse;
import com.task_managment_api.demo.repository.TeamRepository;
import com.task_managment_api.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        User currentUser = getCurrentUser();

        List<User> members = new ArrayList<>();
        members.add(currentUser); // Creator is automatically a member

        if (request.getMemberEmails() != null) {
            for (String email : request.getMemberEmails()) {
                if (email.trim().equalsIgnoreCase(currentUser.getEmail())) {
                    continue;
                }
                userRepository.findByEmail(email.trim()).ifPresent(members::add);
            }
        }

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creator(currentUser)
                .members(members)
                .build();

        Team savedTeam = teamRepository.save(team);
        return TeamResponse.fromEntity(savedTeam);
    }

    public List<TeamResponse> getTeamsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Team> teams = teamRepository.findByMembersContaining(currentUser);
        return teams.stream()
                .map(TeamResponse::fromEntity)
                .toList();
    }

    public TeamResponse getTeamById(Long id) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getMembers().contains(currentUser) && !team.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You are not a member of this team");
        }

        return TeamResponse.fromEntity(team);
    }

    @Transactional
    public TeamResponse addMemberToTeam(Long teamId, String email) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Only creator can manage team members
        if (!team.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: Only the team creator can add members.");
        }

        User userToAdd = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (team.getMembers().contains(userToAdd)) {
            throw new RuntimeException("User is already a member of this team.");
        }

        team.getMembers().add(userToAdd);
        Team savedTeam = teamRepository.save(team);
        return TeamResponse.fromEntity(savedTeam);
    }

    @Transactional
    public TeamResponse removeMemberFromTeam(Long teamId, Long userId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Creator can remove anyone; members can leave themselves
        boolean isCreator = team.getCreator().getId().equals(currentUser.getId());
        boolean isSelfRemoving = currentUser.getId().equals(userId);

        if (!isCreator && !isSelfRemoving) {
            throw new RuntimeException("Access denied: You cannot remove this member.");
        }

        if (team.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Cannot remove the creator of the team.");
        }

        team.getMembers().removeIf(m -> m.getId().equals(userId));
        Team savedTeam = teamRepository.save(team);
        return TeamResponse.fromEntity(savedTeam);
    }
}
