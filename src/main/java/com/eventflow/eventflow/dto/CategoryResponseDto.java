package com.eventflow.eventflow.dto;


import com.eventflow.eventflow.model.Event;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {

    private Long id;
    private String name;

}
