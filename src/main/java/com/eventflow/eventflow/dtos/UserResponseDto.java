package com.eventflow.eventflow.dtos;
import com.eventflow.eventflow.model.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
