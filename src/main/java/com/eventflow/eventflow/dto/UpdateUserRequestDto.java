package com.eventflow.eventflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    private String firstName;

    @NotBlank(message = "lastName cant be empty")
    private String lastName;

    @Email(message = "email must be a valid Email format")
    private String email;
}
