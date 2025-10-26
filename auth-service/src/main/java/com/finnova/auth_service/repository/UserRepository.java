package com.finnova.auth_service.repository;

import com.finnova.auth_service.model.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return Mono of User if found, empty Mono otherwise
     */
    Mono<User> findByUsername(String username);

    /**
     * Finds a user by email.
     *
     * @param email the email to search for
     * @return Mono of User if found, empty Mono otherwise
     */
    Mono<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username to check
     * @return Mono of Boolean - true if exists, false otherwise
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email to check
     * @return Mono of Boolean - true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);
}
