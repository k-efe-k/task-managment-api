package com.task_managment_api.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Email(message = "Please enter a valid e-mail")
    @NotBlank(message = "The name can not empty")
    private String email;

    @NotBlank(message = "The name can not empty")
    private String password;
}
