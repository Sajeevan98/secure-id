package com.sajee.auth.common.api;

import java.util.List;

public record ApiError(
        String code,
        String message,
        String path,
        List<String> errors
) {
}
