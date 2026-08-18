package com.sajee.auth.rbac;

import com.sajee.auth.enums.Permission;
import com.sajee.auth.enums.Role;
import com.sajee.auth.security.rbac.RolePermissions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RolePermissionsTest {

    @Test
    void userShouldHaveAccountPermissions() {

        Set<Permission> permissions = RolePermissions.getPermissions(Role.USER);

        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        Permission.ACCOUNT_READ,
                        Permission.ACCOUNT_UPDATE
                );
    }

    @Test
    void adminShouldHaveAllPermissions() {

        Set<Permission> permissions =
                RolePermissions.getPermissions(Role.ADMIN);

        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        Permission.ACCOUNT_READ,
                        Permission.ACCOUNT_UPDATE,
                        Permission.ROLE_MANAGE
                );
    }

    @Test
    void userShouldNotHaveRoleManagementPermission() {

        Set<Permission> permissions =
                RolePermissions.getPermissions(Role.USER);

        assertThat(permissions)
                .doesNotContain(Permission.ROLE_MANAGE);
    }

}
