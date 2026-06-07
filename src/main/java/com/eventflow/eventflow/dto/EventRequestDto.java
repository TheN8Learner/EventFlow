package com.eventflow.eventflow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 120, message = "Title must be 120 characters or less")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be 2000 characters or less")
    private String description;

    @NotBlank(message = "flyer is required")
    @Size(max = 500, message = "flyer URL must be 500 characters or less")
    @Pattern(regexp = "^https://.+", message = "flyer must be an HTTPS URL")
    private String flyer;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Long capacityMax;

    @NotNull(message = "categoryIds cant be empty")
    @Size(min = 1, max = 10, message = "Select between 1 and 10 categories")
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
