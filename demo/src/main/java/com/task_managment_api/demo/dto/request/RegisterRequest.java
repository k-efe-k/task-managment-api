package com.task_managment_api.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "The name can not empty")
    private String fullName;

    @Email(message = "Please enter a valid e-mail")
    @NotBlank(message = "The name can not empty")
    private String email;

    @Size(min = 6, message = "Password must at least 6 character")
    @NotBlank(message = "The name can not empty")
    private String password;
}
