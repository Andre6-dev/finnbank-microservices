package com.finnova.auth_service.service;

import com.finnova.auth_service.model.dto.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    /**
     * Finds all users.
     *
     * @return Flux of UserDto
     */
    Flux<UserDto> findAll();

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Mono of UserDto
     */
    Mono<UserDto> findById(String id);

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return Mono of UserDto
     */
    Mono<UserDto> findByUsername(String username);

    /**
     * Updates a user.
     *
     * @param id the user ID
     * @param userDto the user data to update
     * @return Mono of updated UserDto
     */
    Mono<UserDto> update(String id, UserDto userDto);

    /**
     * Deletes a user.
     *
     * @param id the user ID
     * @return Mono of Void
     */
    Mono<Void> delete(String id);
}
