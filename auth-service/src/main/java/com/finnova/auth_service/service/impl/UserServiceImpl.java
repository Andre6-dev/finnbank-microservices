package com.finnova.auth_service.service.impl;

import com.finnova.auth_service.exception.UserNotFoundException;
import com.finnova.auth_service.mapper.UserMapper;
import com.finnova.auth_service.model.dto.UserDto;
import com.finnova.auth_service.repository.UserRepository;
import com.finnova.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Flux<UserDto> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll()
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserDto> findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .switchIfEmpty(Mono.error(new UserNotFoundException(
                        "User not found with ID: " + id)));
    }

    @Override
    public Mono<UserDto> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDto)
                .switchIfEmpty(Mono.error(new UserNotFoundException(
                        "User not found with username: " + username)));
    }

    @Override
    public Mono<UserDto> update(String id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(
                        "User not found with ID: " + id)))
                .flatMap(existingUser -> {
                    if (userDto.getEmail() != null) {
                        existingUser.setEmail(userDto.getEmail());
                    }
                    if (userDto.getRoles() != null) {
                        existingUser.setRoles(userDto.getRoles());
                    }
                    if (userDto.getActive() != null) {
                        existingUser.setActive(userDto.getActive());
                    }
                    existingUser.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(existingUser);
                })
                .map(userMapper::toDto)
                .doOnSuccess(updated -> log.info("User updated successfully: {}", id));
    }

    @Override
    public Mono<Void> delete(String id) {
        log.info("Deleting user with ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(
                        "User not found with ID: " + id)))
                .flatMap(userRepository::delete)
                .doOnSuccess(v -> log.info("User deleted successfully: {}", id));
    }
}
