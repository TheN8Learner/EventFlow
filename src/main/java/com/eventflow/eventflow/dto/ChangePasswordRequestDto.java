package com.eventflow.eventflow.dto;

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
public class ChangePasswordRequestDto {

    @NotBlank(message = "currentPassword cant be empty")
    @Size(max = 128, message = "currentPassword must be 128 characters or less")
    private String currentPassword;

    @NotBlank(message = "newPassword cant be empty")
    @Size(min = 8, max = 128, message = "newPassword must be between 8 and 128 characters")
    private String newPassword;

}
