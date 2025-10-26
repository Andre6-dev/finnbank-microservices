package com.finnova.auth_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * User ID.
     */
    private String id;

    /**
     * Username.
     */
    private String username;

    /**
     * Email address.
     */
    private String email;

    /**
     * User roles.
     */
    private List<String> roles;

    /**
     * Account active status.
     */
    private Boolean active;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}
