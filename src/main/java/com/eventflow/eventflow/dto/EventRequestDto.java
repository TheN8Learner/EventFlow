package com.eventflow.eventflow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class EventRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "flyer is required")
    private String flyer;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Long capacityMax;

    @NotNull(message = "categoryIds cant be empty")
    private List<Long> categoryIds;

    public EventRequestDto(String title, String description,String flyer, LocalDateTime date, Long capacityMax, List<Long> categoryIds) {
        this.title = title;
        this.description = description;
        this.flyer = flyer;
        this.date = date;
        this.capacityMax = capacityMax;
        this.categoryIds = categoryIds;
    }
}