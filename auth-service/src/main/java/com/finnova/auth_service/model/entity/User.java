package com.finnova.auth_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    private String id;

    /**
     * Username for authentication.
     * Must be unique across the system.
     */
    @Indexed(unique = true)
    private String username;

    /**
     * Encrypted password.
     */
    private String password;

    /**
     * Email address of the user.
     * Must be unique across the system.
     */
    @Indexed(unique = true)
    private String email;

    /**
     * List of roles assigned to the user.
     * Examples: USER, ADMIN, MANAGER
     */
    private List<String> roles;

    /**
     * Indicates if the user account is active.
     */
    private Boolean active;

    /**
     * Timestamp when the user was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user was last updated.
     */
    private LocalDateTime updatedAt;
}
