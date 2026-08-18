package com.sajee.auth.controller;

import com.sajee.auth.admin.AdminAccountService;
import com.sajee.auth.common.api.ApiEndpoints;
import com.sajee.auth.common.api.ApiResponse;
import com.sajee.auth.dto.request.UpdateAccountRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiEndpoints.ADMIN)
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PatchMapping("/{uuid}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> changeRole(
            @PathVariable UUID uuid, @Valid @RequestBody UpdateAccountRoleRequest request) {

        adminAccountService.changeRole(uuid, request.role());

        return ApiResponse.success("Role changed successfully for "+ uuid);
    }
}
