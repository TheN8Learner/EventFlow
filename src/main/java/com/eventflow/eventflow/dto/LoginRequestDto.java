package com.eventflow.eventflow.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @Email(message = "email need to be a valid Email format")
    private String email;
    @NotBlank(message = "password cant be empty")
    private String password;
}
