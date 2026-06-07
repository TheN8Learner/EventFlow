package com.eventflow.eventflow.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "email cant be empty")
    @Email(message = "email need to be a valid Email format")
    @Size(max = 254, message = "email must be 254 characters or less")
    private String email;
    @NotBlank(message = "password cant be empty")
    @Size(max = 128, message = "password must be 128 characters or less")
    private String password;
}
