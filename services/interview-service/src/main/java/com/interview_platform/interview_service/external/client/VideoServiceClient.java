package com.interview_platform.interview_service.external.client;

import com.interview_platform.interview_service.dto.ApiResponse;
import com.interview_platform.interview_service.external.dto.CreateRoomRequest;
import com.interview_platform.interview_service.external.dto.RoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "call-service", url = "${feign.client.config.call-service.url}")
public interface VideoServiceClient {

    @PostMapping("/api/call/rooms")
    ApiResponse<RoomResponse> createVideoRoom(@RequestBody CreateRoomRequest request);

    @DeleteMapping("/api/video/rooms/{roomId}")
    void deleteVideoRoom(@PathVariable String roomId);
}