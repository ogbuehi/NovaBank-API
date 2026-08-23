package com.bankapi.NovaBank.API.mapper;

import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.entity.Account;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = UserMapper.class
)
public interface AccountMapper {

    AccountResponse toResponse(Account account);

}