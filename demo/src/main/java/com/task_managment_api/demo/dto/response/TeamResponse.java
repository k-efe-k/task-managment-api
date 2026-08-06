package com.task_managment_api.demo.dto.response;

import com.task_managment_api.demo.domain.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private String creatorName;
    private List<MemberDto> members;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDto {
        private Long id;
        private String fullName;
        private String email;
    }

    public static TeamResponse fromEntity(Team team) {
        if (team == null) return null;
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .creatorId(team.getCreator().getId())
                .creatorName(team.getCreator().getFullName())
                .members(team.getMembers().stream()
                        .map(m -> MemberDto.builder()
                                .id(m.getId())
                                .fullName(m.getFullName())
                                .email(m.getEmail())
                                .build())
                        .toList())
                .build();
    }
}
