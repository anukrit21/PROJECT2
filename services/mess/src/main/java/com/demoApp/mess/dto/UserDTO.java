package com.demoApp.mess.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.demoApp.mess.entity.User;
import com.demoApp.mess.enums.RoleType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data  // Add @Data for automatic getters/setters
public class UserDTO {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    private String address;
    private RoleType role; 
    private boolean active;
    private boolean enabled;  
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
    
        
        RoleType roleType = RoleType.valueOf(user.getRole().name());
 
    
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .address(user.getAddress())
                .role(roleType)
                .active(user.isActive())
                .enabled(user.isEnabled())  
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}    