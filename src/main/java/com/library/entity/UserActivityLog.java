package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@Data
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SystemUser user;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String description;
    private String ipAddress;

    @CreatedDate
    private LocalDateTime timestamp;

    public enum ActivityType {
        LOGIN, LOGOUT, CREATE_BOOK, UPDATE_BOOK, DELETE_BOOK,
        CREATE_MEMBER, UPDATE_MEMBER, DELETE_MEMBER,
        ISSUE_BOOK, RETURN_BOOK, CREATE_USER, UPDATE_USER, DELETE_USER
    }
}
