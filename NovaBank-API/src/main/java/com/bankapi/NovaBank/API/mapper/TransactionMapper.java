package com.bankapi.NovaBank.API.mapper;

import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(
            target = "senderAccount",
            source = "senderAccount.accountNumber"
    )
    @Mapping(
            target = "receiverAccount",
            source = "receiverAccount.accountNumber"
    )
    TransactionResponse toResponse(Transaction transaction);
    List<TransactionResponse> toResponse(List<Transaction> transactions);
}