package com.eventflow.eventflow.dto;


import com.eventflow.eventflow.model.Category;
import com.eventflow.eventflow.model.EventStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {

    private Long id;
    private String title;
    private String description;
    private String flyer;
    private LocalDateTime date;
    private Long capacityMax;
    private EventStatus status;
    private Long creatorId;
    private String creatorName;
    private String creatorEmail;
    private List<CategoryResponseDto> categories;

}