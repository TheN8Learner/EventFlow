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
    private String userName;
    private String userEmail;
    private String eventTitle;

    public RegistrationResponseDto(Long id, RegistrationStatus status, Long eventId, Long userId) {
        this.id = id;
        this.status = status;
        this.eventId = eventId;
        this.userId = userId;
    }

}
