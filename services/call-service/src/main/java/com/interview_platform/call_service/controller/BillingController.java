//package com.interview_platform.call_service.controller;
//
//import com.interview_platform.call_service.dto.QualityStatistics;
//import com.interview_platform.call_service.dto.RealtimeQualityStatistics;
//import com.interview_platform.call_service.entity.InterviewRoom;
//import com.interview_platform.call_service.dto.InvoiceDto;
//import com.interview_platform.call_service.entity.MinuteQualityLog;
//import com.interview_platform.call_service.service.BillingService;
//import com.interview_platform.call_service.service.QualityCacheService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * HYBRID APPROACH - Billing Controller
// *
// * Provides endpoints for:
// * 1. Real-time data (from Redis) - during active call
// * 2. Historical data (from Database) - after call ends
// */
//@RestController
//@RequestMapping("/api/billing")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class BillingController {
//
//    private final BillingService billingService;
//    private final QualityCacheService qualityCacheService;
//
//    /**
//     * Start billing session for a room
//     * Called when the interview actually starts (both parties joined)
//     * Initializes Redis cache
//     */
//    @PostMapping("/sessions/{roomId}/start")
//    public ResponseEntity<Map<String, Object>> startSession(@PathVariable String roomToken) {
//        billingService.startBillingSession(roomToken);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Billing session started - data will be cached in Redis");
//        response.put("roomToken", roomToken);
//        response.put("timestamp", System.currentTimeMillis());
//        response.put("note", "Data will be written to database when call ends");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * End billing session and BATCH WRITE to database
//     * This is the critical method that persists all Redis data to database
//     */
//    @PostMapping("/sessions/{roomId}/end")
//    public ResponseEntity<Map<String, Object>> endSession(@PathVariable String roomToken) {
//        // This will:
//        // 1. Retrieve all minute data from Redis
//        // 2. Batch write to database
//        // 3. Calculate final billing
//        // 4. Clean up Redis cache
//        InterviewRoom room = billingService.endBillingSession(roomToken);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("room", room);
//        response.put("message", "Billing session ended - all data written to database");
//        response.put("summary", Map.of(
//                "totalMinutes", room.getActualDurationMinutes(),
//                "goodMinutes", room.getGoodQualityMinutes(),
//                "poorMinutes", room.getPoorQualityMinutes(),
//                "scheduledAmount", room.getScheduledAmount(),
//                "finalAmount", room.getFinalAmount(),
//                "discount", room.getQualityDiscount()
//        ));
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Get REAL-TIME quality statistics (from Redis)
//     * Use this during active call for monitoring dashboard
//     */
//    @GetMapping("/sessions/{roomId}/realtime-stats")
//    public ResponseEntity<RealtimeQualityStatistics> getRealtimeStats(
//            @PathVariable UUID roomId) {
//
//        RealtimeQualityStatistics stats =
//                billingService.getRealtimeQualityStatistics(roomId);
//
//        return ResponseEntity.ok(stats);
//    }
//
//    /**
//     * Get HISTORICAL quality statistics (from Database)
//     * Use this after call ends for reports and analytics
//     */
//    @GetMapping("/sessions/{roomId}/stats")
//    public ResponseEntity<QualityStatistics> getQualityStats(
//            @PathVariable String roomToken) {
//
//        QualityStatistics stats =
//                billingService.getQualityStatistics(roomToken);
//
//        return ResponseEntity.ok(stats);
//    }
//
//    /**
//     * Get minute-by-minute breakdown (from Database)
//     * Only available AFTER call ends
//     */
//    @GetMapping("/sessions/{roomId}/breakdown")
//    public ResponseEntity<Map<String, Object>> getBreakdown(@PathVariable String roomToken) {
//        List<MinuteQualityLog> breakdown = billingService.getMinuteBreakdown(roomToken);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("breakdown", breakdown);
//        response.put("totalMinutes", breakdown.size());
//        response.put("source", "database");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Get minute breakdown for specific user (from Database)
//     */
//    @GetMapping("/sessions/{roomId}/users/{userId}/breakdown")
//    public ResponseEntity<List<MinuteQualityLog>> getUserBreakdown(
//            @PathVariable String roomToken,
//            @PathVariable Long userId) {
//
//        List<MinuteQualityLog> breakdown =
//                billingService.getUserMinuteBreakdown(roomToken, userId);
//
//        return ResponseEntity.ok(breakdown);
//    }
//
//    /**
//     * Generate and get invoice (from Database)
//     * Only available AFTER call ends
//     */
//    @GetMapping("/sessions/{roomId}/invoice")
//    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable String roomToken) {
//        InvoiceDto invoice = billingService.generateInvoice(roomToken);
//        return ResponseEntity.ok(invoice);
//    }
//
//    /**
//     * Get invoice as formatted text (from Database)
//     */
//    @GetMapping("/sessions/{roomId}/invoice/text")
//    public ResponseEntity<String> getInvoiceText(@PathVariable String roomToken) {
//        InvoiceDto invoice = billingService.generateInvoice(roomToken);
//        return ResponseEntity.ok(invoice.getInvoiceSummary());
//    }
//
//    /**
//     * Get invoice summary (from Database)
//     */
//    @GetMapping("/sessions/{roomId}/invoice/summary")
//    public ResponseEntity<InvoiceDto.InvoiceSummary> getInvoiceSummary(
//            @PathVariable String roomToken) {
//
//        InvoiceDto invoice = billingService.generateInvoice(roomToken);
//        return ResponseEntity.ok(invoice.getSummary());
//    }
//
//    /**
//     * Get Redis cache summary for debugging
//     * Shows what's currently cached in Redis for a room
//     */
//    @GetMapping("/sessions/{roomId}/cache-summary")
//    public ResponseEntity<Map<String, Object>> getCacheSummary(@PathVariable UUID roomId) {
//        Map<String, Integer> cacheSummary =
//                qualityCacheService.getRoomCacheSummary(roomId.toString());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("roomId", roomId);
//        response.put("cacheSummary", cacheSummary);
//        response.put("note", "This shows data currently in Redis (not yet written to database)");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Get connection states for all participants (from Redis)
//     * Real-time connection status
//     */
//    @GetMapping("/sessions/{roomId}/connection-states")
//    public ResponseEntity<Map<Long, String>> getConnectionStates(
//            @PathVariable UUID roomId,
//            @RequestParam List<Long> userIds) {
//
//        Map<Long, String> states = new HashMap<>();
//        String roomIdStr = roomId.toString();
//
//        for (Long userId : userIds) {
//            String state = qualityCacheService.getConnectionState(roomIdStr, userId);
//            states.put(userId, state);
//        }
//
//        return ResponseEntity.ok(states);
//    }
//
//    /**
//     * Get recent quality alerts (from Redis)
//     * Real-time alerts during call
//     */
//    @GetMapping("/sessions/{roomId}/alerts")
//    public ResponseEntity<Map<String, Object>> getAlerts(@PathVariable UUID roomId) {
//        List<Object> alerts = qualityCacheService.getQualityAlerts(roomId.toString());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("alerts", alerts);
//        response.put("count", alerts.size());
//        response.put("source", "redis (real-time)");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Get quality trend for a user (from Redis)
//     * Recent quality samples for real-time graphing
//     */
//    @GetMapping("/sessions/{roomId}/users/{userId}/trend")
//    public ResponseEntity<Map<String, Object>> getQualityTrend(
//            @PathVariable UUID roomId,
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "20") int count) {
//
//        List<Object> samples = qualityCacheService.getQualitySamples(
//                roomId.toString(), userId, count);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("samples", samples);
//        response.put("count", samples.size());
//        response.put("source", "redis (real-time)");
//        response.put("note", "Last " + count + " 5-second samples");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Force cleanup Redis cache (admin endpoint)
//     * Use only if call ended abnormally
//     */
//    @DeleteMapping("/sessions/{roomId}/cache")
//    public ResponseEntity<Map<String, String>> cleanupCache(@PathVariable UUID roomId) {
//        qualityCacheService.cleanupRoomCache(roomId.toString());
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "Redis cache cleaned up for room " + roomId);
//        response.put("warning", "This should only be used if call ended abnormally");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Health check endpoint
//     */
//    @GetMapping("/health")
//    public ResponseEntity<Map<String, String>> health() {
//        Map<String, String> response = new HashMap<>();
//        response.put("status", "UP");
//        response.put("service", "billing (hybrid approach)");
//        response.put("approach", "Redis during call → Database at end");
//
//        return ResponseEntity.ok(response);
//    }
//}