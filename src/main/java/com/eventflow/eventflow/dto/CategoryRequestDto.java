package com.eventflow.eventflow.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "name cant be empty")
    @Size(max = 60, message = "name must be 60 characters or less")
    private String name;
}
