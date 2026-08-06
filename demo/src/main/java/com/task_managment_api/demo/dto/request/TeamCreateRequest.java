package com.task_managment_api.demo.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TeamCreateRequest {
    private String name;
    private String description;
    private List<String> memberEmails;
}
