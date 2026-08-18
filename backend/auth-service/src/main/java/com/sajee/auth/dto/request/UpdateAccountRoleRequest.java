package com.sajee.auth.dto.request;

import com.sajee.auth.enums.Role;

public record UpdateAccountRoleRequest(
        Role role
) {
}
