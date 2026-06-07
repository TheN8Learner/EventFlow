package com.eventflow.eventflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"creator", "eventRoles"})
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String flyer;

    private LocalDateTime date;

    @Column(name = "capacity_max")
    private Long capacityMax;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "event")
    private List<UserEventRole> eventRoles;

    @ManyToMany
    @JoinTable(
            name = "event_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @OneToMany(mappedBy = "event")
    private List<Registration> registrations;

    public Event(String title, String description, String flyer, LocalDateTime date, Long capacityMax, EventStatus status,  User creator, List<Category> categories) {
        this.title = title;
        this.description = description;
        this.flyer = flyer;
        this.date = date;
        this.capacityMax = capacityMax;
        this.status = status;
        this.creator = creator;
        this.categories = categories;
    }
}