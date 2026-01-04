package com.interview_platform.interview_service.external.client;

import com.interview_platform.interview_service.external.dto.CreateVideoRoomRequest;
import com.interview_platform.interview_service.external.dto.VideoRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "video-service", url = "${feign.client.config.video-service.url}")
public interface VideoServiceClient {

    @PostMapping("/api/video/rooms")
    VideoRoomResponse createVideoRoom(@RequestBody CreateVideoRoomRequest request);

    @DeleteMapping("/api/video/rooms/{roomId}")
    void deleteVideoRoom(@PathVariable String roomId);
}