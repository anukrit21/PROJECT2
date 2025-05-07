package com.demoApp.common_kafka.event.user;

import lombok.Data;

@Data
public class UserPreference {
    private String preferenceType;
    private String preferenceValue;
}
