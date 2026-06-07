package com.eventflow.eventflow.dto;

import com.eventflow.eventflow.model.RegistrationStatus;
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
public class RegistrationResponseDto {

    private Long id;
    private RegistrationStatus status;
    private Long eventId;
    private Long userId;

}
