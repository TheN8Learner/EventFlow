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
    private Long registeredCount;
    private Long availableSeats;

    public EventResponseDto(
            Long id,
            String title,
            String description,
            String flyer,
            LocalDateTime date,
            Long capacityMax,
            EventStatus status,
            Long creatorId,
            String creatorName,
            String creatorEmail,
            List<CategoryResponseDto> categories
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.flyer = flyer;
        this.date = date;
        this.capacityMax = capacityMax;
        this.status = status;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.creatorEmail = creatorEmail;
        this.categories = categories;
    }

}
