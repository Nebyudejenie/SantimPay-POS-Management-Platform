package com.santimpay.posctl.notification.web;

import com.santimpay.posctl.notification.application.NotificationService;
import com.santimpay.posctl.notification.application.NotificationStream;
import com.santimpay.posctl.notification.domain.Notification;
import com.santimpay.posctl.shared.security.CurrentUser;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final NotificationStream stream;

    public record NotificationResponse(UUID id, String channel, String template, String payload,
                                       String status, String relatedType, UUID relatedId,
                                       Instant createdAt, Instant readAt) {}

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getChannel().name(), n.getTemplate(),
                n.getPayload(), n.getStatus().name(), n.getRelatedType(), n.getRelatedId(),
                n.getAudit().getCreatedAt(), n.getReadAt());
    }

    @Operation(summary = "My notification inbox")
    @GetMapping("/notifications")
    public PageResponse<NotificationResponse> inbox(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<Notification> result = service.myInbox(unreadOnly, PageRequest.of(page, Math.min(limit, 200)));
        return PageResponse.from(result, result.map(this::toResponse).getContent());
    }

    @Operation(summary = "Mark a notification read")
    @PostMapping("/notifications/{id}:read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        return toResponse(service.markRead(id));
    }

    @Operation(summary = "Live notification stream (Server-Sent Events)")
    @GetMapping(path = "/stream/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        UUID me = CurrentUser.id().orElse(new UUID(0, 0));
        return stream.subscribe(me);
    }
}
