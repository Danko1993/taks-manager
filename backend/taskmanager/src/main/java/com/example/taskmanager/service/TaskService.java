package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public List<TaskResponse> getAllTasks(UUID userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public TaskResponse getTask(UUID id, UUID userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .map(taskMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public TaskResponse createTask(TaskRequest request, User user) {
        Task task = taskMapper.toEntity(request);
        task.setUser(user);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(UUID id, TaskRequest request, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDeadline(request.deadline());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public void deleteTask(UUID id, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    public TaskResponse toggleDone(UUID id, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setDone(!task.isDone());
        return taskMapper.toResponse(taskRepository.save(task));
    }
}