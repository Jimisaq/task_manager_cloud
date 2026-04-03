package org.jimisaac.task_manager_cloud_assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jimisaac.task_manager_cloud_assignment.dto.TaskRequest;
import org.jimisaac.task_manager_cloud_assignment.dto.TaskResponse;
import org.jimisaac.task_manager_cloud_assignment.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        logger.info("======== POST /api/tasks ENDPOINT HIT ========");
        logger.info("POST /api/tasks called with request: {}", request);
        TaskResponse response = taskService.createTask(request);
        logger.info("Task created successfully with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        logger.info("GET /api/tasks called");
        List<TaskResponse> tasks = taskService.getAllTasks();
        logger.info("Retrieved {} tasks", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        logger.info("GET /api/tasks/{} called", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request) {
        logger.info("PUT /api/tasks/{} called with request: {}", id, request);
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        logger.info("DELETE /api/tasks/{} called", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        logger.info("OPTIONS /api/tasks called - CORS preflight request");
        return ResponseEntity.ok().build();
    }
}
