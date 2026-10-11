package edu.br.resource.resourcesystem.controller.shared;

import edu.br.resource.resourcesystem.service.notification.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;

    @GetMapping
    public ResponseEntity<NotificationService.Inbox> inbox(
            Authentication auth, @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(notifications.inbox(notifications.owner(auth), page));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> read(Authentication auth, @PathVariable UUID id) {
        notifications.read(notifications.owner(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(Authentication auth) {
        notifications.readAll(notifications.owner(auth));
        return ResponseEntity.noContent().build();
    }
}
