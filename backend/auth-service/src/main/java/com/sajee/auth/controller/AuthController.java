package com.sajee.auth.controller;

import com.sajee.auth.common.api.ApiEndpoints;
import com.sajee.auth.common.api.ApiResponse;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> registration(@Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = accountService.register(request);
        return ApiResponse.success(response);
    }



}
