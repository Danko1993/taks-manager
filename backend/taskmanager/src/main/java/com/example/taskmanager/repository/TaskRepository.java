package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserId(UUID userId);

    List<Task> findByDone(boolean done);

    List<Task> findByUserIdAndDone(UUID userId, boolean done);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);
}