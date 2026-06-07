package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Priority priority,
        boolean done,
        LocalDateTime deadline,
        LocalDateTime createdAt
) {}