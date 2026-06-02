package com.santimpay.posctl.tasks.infrastructure;

import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TaskJpaRepository extends JpaRepository<Task, UUID> {

    @Query("""
           select t from Task t
           where t.audit.deletedAt is null
             and (:status is null or t.status = :status)
             and (:assigneeId is null or t.assigneeId = :assigneeId)
             and (:taskType is null or t.taskType = :taskType)
           """)
    Page<Task> search(@Param("status") TaskStatus status,
                      @Param("assigneeId") UUID assigneeId,
                      @Param("taskType") String taskType,
                      Pageable pageable);
}
