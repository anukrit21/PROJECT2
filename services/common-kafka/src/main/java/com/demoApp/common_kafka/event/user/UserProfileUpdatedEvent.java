package com.demoApp.common_kafka.event.user;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserProfileUpdatedEvent extends BaseEvent {

    private UUID userId;
    private UUID oldPictureId;
    private UUID newPictureId;
    private String oldEmail;
    private String newEmail;
    private String oldPhone;
    private String newPhone;
    private String oldFirstName;
    private String newFirstName;
    private String oldLastName;
    private String newLastName;
    private LocalDate oldDateOfBirth;
    private LocalDate newDateOfBirth;
    private String oldGender;
    private String newGender;
    private Map<String, String> updatedFields;
    private List<UserAddress> updatedAddresses;
    private List<UserPreference> updatedPreferences;
    private LocalDateTime updatedAt;
    private String updatedBy; // USER, ADMIN, SYSTEM

    /**
     * Constructor with initialization
     */
    public UserProfileUpdatedEvent(UUID userId, UUID oldPictureId, UUID newPictureId,
                                   String oldEmail, String newEmail, String oldPhone, String newPhone,
                                   String oldFirstName, String newFirstName, String oldLastName, String newLastName,
                                   LocalDate oldDateOfBirth, LocalDate newDateOfBirth, String oldGender, String newGender,
                                   Map<String, String> updatedFields, List<UserAddress> updatedAddresses,
                                   List<UserPreference> updatedPreferences, LocalDateTime updatedAt, String updatedBy) {
        super();
        init("USER_PROFILE_UPDATED", "user-service");
        this.userId = userId;
        this.oldPictureId = oldPictureId;
        this.newPictureId = newPictureId;
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
        this.oldPhone = oldPhone;
        this.newPhone = newPhone;
        this.oldFirstName = oldFirstName;
        this.newFirstName = newFirstName;
        this.oldLastName = oldLastName;
        this.newLastName = newLastName;
        this.oldDateOfBirth = oldDateOfBirth;
        this.newDateOfBirth = newDateOfBirth;
        this.oldGender = oldGender;
        this.newGender = newGender;
        this.updatedFields = updatedFields;
        this.updatedAddresses = updatedAddresses;
        this.updatedPreferences = updatedPreferences;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}
