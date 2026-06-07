package com.eventflow.eventflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateUserRequestDto {

    @NotBlank(message = "firstName cant be empty")
    @Size(max = 80, message = "firstName must be 80 characters or less")
    private String firstName;

    @NotBlank(message = "lastName cant be empty")
    @Size(max = 80, message = "lastName must be 80 characters or less")
    private String lastName;

    @NotBlank(message = "email cant be empty")
    @Email(message = "email must be a valid Email format")
    @Size(max = 254, message = "email must be 254 characters or less")
    private String email;
}
