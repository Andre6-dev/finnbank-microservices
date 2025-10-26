package com.finnova.auth_service.controller;

import com.finnova.auth_service.model.dto.UserDto;
import com.finnova.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets all users.
     *
     * @return Flux of UserDto
     */
    @GetMapping
    public Flux<UserDto> getAllUsers() {
        log.info("GET /users - Getting all users");
        return userService.findAll()
                .doOnComplete(() -> log.info("Retrieved all users successfully"));
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return Mono of ResponseEntity with UserDto
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable String id) {
        log.info("GET /users/{} - Getting user by ID", id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User retrieved successfully: {}", id))
                .doOnError(e -> log.error("Error retrieving user: {}", e.getMessage()));
    }

    /**
     * Gets a user by username.
     *
     * @param username the username
     * @return Mono of ResponseEntity with UserDto
     */
    @GetMapping("/username/{username}")
    public Mono<ResponseEntity<UserDto>> getUserByUsername(@PathVariable String username) {
        log.info("GET /users/username/{} - Getting user by username", username);
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User retrieved successfully: {}", username))
                .doOnError(e -> log.error("Error retrieving user: {}", e.getMessage()));
    }

    /**
     * Updates a user.
     *
     * @param id the user ID
     * @param userDto the user data to update
     * @return Mono of ResponseEntity with updated UserDto
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDto>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserDto userDto) {
        log.info("PUT /users/{} - Updating user", id);
        return userService.update(id, userDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating user: {}", e.getMessage()));
    }

    /**
     * Deletes a user.
     *
     * @param id the user ID
     * @return Mono of ResponseEntity
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        log.info("DELETE /users/{} - Deleting user", id);
        return userService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(r -> log.info("User deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting user: {}", e.getMessage()));
    }
}
