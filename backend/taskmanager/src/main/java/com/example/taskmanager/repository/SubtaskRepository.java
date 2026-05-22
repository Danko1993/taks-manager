package com.example.taskmanager.repository;

import com.example.taskmanager.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    List<Subtask> findByTaskId(UUID taskId);
}