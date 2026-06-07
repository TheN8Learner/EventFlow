package com.eventflow.eventflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RegisterRequestDto {

    @NotBlank(message = "firstName cant be empty")
    @Size(max = 80, message = "firstName must be 80 characters or less")
    private String firstName;
    @NotBlank(message = "lastName cant be empty")
    @Size(max = 80, message = "lastName must be 80 characters or less")
    private String lastName;
    @NotBlank(message = "email cant be empty")
    @Email(message = "email need to be a valid Email format")
    @Size(max = 254, message = "email must be 254 characters or less")
    private String email;
    @NotBlank(message = "password cant be empty")
    @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
    private String password;
}
