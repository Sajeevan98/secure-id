package com.sajee.auth.dto.response;

import com.sajee.auth.entity.Account;
import com.sajee.auth.enums.Permission;
import com.sajee.auth.enums.Role;

import java.util.UUID;

public record RegisterResponse(

        UUID uuid,
        String username,
        String email,
        String status,
        boolean emailVerified,
        Role role
) {

    public static RegisterResponse from(Account account) {
        return new RegisterResponse(
                account.getUuid(),
                account.getUsername(),
                account.getEmail(),
                account.getStatus().name(),
                account.isEmailVerified(),
                account.getRole()
        );
    }
}
