package com.finnova.auth_service.mapper;

import com.finnova.auth_service.model.dto.RegisterRequest;
import com.finnova.auth_service.model.dto.UserDto;
import com.finnova.auth_service.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Converts User entity to UserDto.
     *
     * @param user the user entity
     * @return the user DTO
     */
    UserDto toDto(User user);

    /**
     * Converts RegisterRequest to User entity.
     * Password field is ignored and will be set separately after encryption.
     *
     * @param request the register request
     * @return the user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}
