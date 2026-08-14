package com.sajee.auth.controller;

import com.sajee.auth.common.api.ApiEndpoints;
import com.sajee.auth.common.api.ApiResponse;
import com.sajee.auth.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiEndpoints.ACCOUNT)
@RequiredArgsConstructor
public class AccountController {

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> account = Map.of(
                "uuid", jwt.getSubject(),
                "username", jwt.getClaimAsString("username")
        );

        return ApiResponse.success(account);
    }
}
