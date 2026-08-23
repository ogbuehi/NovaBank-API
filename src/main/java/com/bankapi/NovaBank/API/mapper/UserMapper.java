package com.bankapi.NovaBank.API.mapper;

import com.bankapi.NovaBank.API.dto.response.UserResponse;
import com.bankapi.NovaBank.API.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

}