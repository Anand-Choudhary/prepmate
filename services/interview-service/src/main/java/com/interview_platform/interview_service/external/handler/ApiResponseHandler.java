package com.interview_platform.interview_service.external.handler;

import com.interview_platform.interview_service.dto.ApiResponse;
import com.interview_platform.interview_service.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseHandler {

    public <T> T unwrap(ApiResponse<T> response) {
        if (response == null) {
            throw new BusinessException("Null response from downstream service");
        }

        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }

        if (response.getData() == null) {
            throw new BusinessException("Empty response data from downstream service");
        }

        return response.getData();
    }
}
