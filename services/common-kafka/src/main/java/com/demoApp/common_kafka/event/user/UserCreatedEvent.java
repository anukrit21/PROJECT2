package com.demoApp.common_kafka.event.user;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {
    private UUID userId;
    private String username;
    private String email;
    private String role;
    
    public UserCreatedEvent(UUID userId, String username, String email, String role, String source) {
        super("USER_CREATED", source);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
} 