package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank String title,
        String description,
        Priority priority,
        LocalDateTime deadline
) {}