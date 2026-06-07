package com.eventflow.eventflow.dtos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthRequestDto {

    @Email(message = "email must be a valid email format")
    private String email;
    @NotBlank(message = "password cant be empty")
    private String password;
}
