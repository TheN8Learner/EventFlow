package com.eventflow.eventflow.dto;

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
public class ChangePasswordRequestDto {

    @NotBlank(message = "currentPassword cant be empty")
    private String currentPassword;

    @NotBlank(message = "newPassword cant be empty")
    private String newPassword;

}
