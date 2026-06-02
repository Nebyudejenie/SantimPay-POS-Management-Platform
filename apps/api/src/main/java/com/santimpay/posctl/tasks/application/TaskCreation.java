package com.santimpay.posctl.tasks.application;

import com.santimpay.posctl.tasks.domain.TaskPriority;
import java.util.UUID;
import org.springframework.modulith.NamedInterface;

/**
 * Published application API for system/event-driven task creation. Exposed as a
 * {@code @NamedInterface} so other modules can spawn work (e.g. a swap task on a faulty device)
 * without depending on tasks internals or going through method-security (these run in system context).
 */
@NamedInterface("api")
public interface TaskCreation {

    UUID createSystemTask(String title, String description, String taskType, TaskPriority priority,
                          String relatedType, UUID relatedId);
}
