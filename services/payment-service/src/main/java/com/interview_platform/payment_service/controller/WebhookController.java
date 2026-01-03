package com.interview_platform.payment_service.controller;

import com.interview_platform.payment_service.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody String payload,
                                                       @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            if ("payment.captured".equals(event)) {
                JSONObject paymentPayload = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String orderId = paymentPayload.getString("order_id");
                String paymentId = paymentPayload.getString("id");

                paymentGatewayService.handlePaymentSuccess(orderId, paymentId, signature);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed");
        }
    }
}