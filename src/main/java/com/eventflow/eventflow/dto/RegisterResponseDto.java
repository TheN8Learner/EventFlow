package com.eventflow.eventflow.dto;

import com.eventflow.eventflow.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegisterResponseDto {

  private String accessToken;
  private String refreshToken;
}
