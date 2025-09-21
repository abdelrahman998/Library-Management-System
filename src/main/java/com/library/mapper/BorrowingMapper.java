package com.library.mapper;

import com.library.dto.BorrowingResponse;
import com.library.entity.BorrowingTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BorrowingMapper {
    BorrowingMapper INSTANCE = Mappers.getMapper(BorrowingMapper.class);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "issuedById", source = "issuedBy.id")
    @Mapping(target = "returnedToId", source = "returnedTo.id")
    BorrowingResponse toResponse(BorrowingTransaction transaction);
}