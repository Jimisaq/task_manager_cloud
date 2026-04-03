package org.jimisaac.task_manager_cloud_assignment.repository;

import org.jimisaac.task_manager_cloud_assignment.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
