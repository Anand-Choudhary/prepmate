package com.interview_platform.interview_service.external.client;

import com.interview_platform.interview_service.external.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${feign.client.config.user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable String userId);

}
