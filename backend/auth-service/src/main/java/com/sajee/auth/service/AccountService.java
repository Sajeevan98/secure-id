package com.sajee.auth.service;

import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.RegisterResponse;

public interface AccountService {

    RegisterResponse register(RegisterRequest request);
}
