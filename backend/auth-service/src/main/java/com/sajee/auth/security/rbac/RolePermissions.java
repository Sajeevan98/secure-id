package com.sajee.auth.security.rbac;

import com.sajee.auth.enums.Permission;
import com.sajee.auth.enums.Role;

import java.util.Set;

public final class RolePermissions {

    private RolePermissions() {
    }

    public static Set<Permission> getPermissions(Role role) {

        return switch (role) {

            case USER -> Set.of(
                    Permission.ACCOUNT_READ,
                    Permission.ACCOUNT_UPDATE
            );

            case ADMIN -> Set.of(
                    Permission.ACCOUNT_READ,
                    Permission.ACCOUNT_UPDATE,
                    Permission.ROLE_MANAGE
            );
        };
    }
}