package com.prepmate.consumer_service.service;


import com.prepmate.consumer_service.dao.NotificationRepository;
import com.prepmate.consumer_service.entity.GenericEvent;
import com.prepmate.consumer_service.entity.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final NotificationRepository logRepository;

    @Value("${platform.name}")
    private String platformName;

    public void handleEvent(GenericEvent event) {
        String eventType = event.getEventType();
        Map<String, Object> payload = event.getPayload();

        log.info("Handling event type: {} with eventId: {}", eventType, event.getEventId());

        switch (eventType) {
            case "USER_REGISTERED" -> handleUserRegistered(payload);
            case "USER_VERIFIED" -> handleUserVerified(payload);
            case "PAYMENT_SUCCESS" -> handlePaymentSuccess(payload);
            case "PAYMENT_FAILED" -> handlePaymentFailed(payload);
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

    // ========== User Events ==========
    private void handleUserRegistered(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String phoneNumber = (String) payload.get("phoneNumber");

        log.info("Handling user registration for: {}", userId);

        // Send Email
        sendEmail(
                userId,
                email,
                "Welcome to " + platformName + ", " + name + "!",
                buildWelcomeEmailBody(name, email),
                "USER_REGISTERED"
        );

        // Send SMS
//        sendSms(
//                userId,
//                phoneNumber,
//                "Welcome " + name + "! Thank you for registering at " + platformName,
//                "USER_REGISTERED"
//        );
    }

    private void handleUserVerified(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");

        log.info("Handling user verification for: {}", userId);

        sendEmail(
                userId,
                email,
                "Account Verified Successfully",
                buildVerificationEmailBody(name),
                "USER_VERIFIED"
        );
    }

    // ========== Payment Events ==========
    private void handlePaymentSuccess(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String orderId = (String) payload.get("orderId");
        Double amount = ((Number) payload.get("amount")).doubleValue();
        String currency = (String) payload.get("currency");
        String transactionId = (String) payload.get("transactionId");

        log.info("Handling payment success for user: {}, transaction: {}", userId, transactionId);

        String email = getUserEmail(userId);
        String phone = getUserPhone(userId);

        sendEmail(
                userId,
                email,
                "Payment Successful - " + transactionId,
                buildPaymentSuccessEmailBody(transactionId, amount, currency, orderId),
                "PAYMENT_SUCCESS"
        );

//        sendSms(
//                userId,
//                phone,
//                String.format("Payment of %.2f %s received. Transaction: %s",
//                        amount, currency, transactionId),
//                "PAYMENT_SUCCESS"
//        );
    }

    private void handlePaymentFailed(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String orderId = (String) payload.get("orderId");
        Double amount = ((Number) payload.get("amount")).doubleValue();
        String currency = (String) payload.get("currency");
        String reason = (String) payload.get("reason");

        log.info("Handling payment failure for user: {}", userId);

        String email = getUserEmail(userId);

        sendEmail(
                userId,
                email,
                "Payment Failed - Action Required",
                buildPaymentFailedEmailBody(amount, currency, orderId, reason),
                "PAYMENT_FAILED"
        );
    }

    // ========== Order Events ==========
    private void handleOrderCreated(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String orderId = (String) payload.get("orderId");
        Double totalAmount = ((Number) payload.get("totalAmount")).doubleValue();
        Integer itemCount = ((Number) payload.get("itemCount")).intValue();

        log.info("Handling order created for user: {}, order: {}", userId, orderId);

        String email = getUserEmail(userId);

        sendEmail(
                userId,
                email,
                "Order Confirmation - #" + orderId,
                buildOrderCreatedEmailBody(orderId, totalAmount, itemCount),
                "ORDER_CREATED"
        );
    }

    private void handleOrderShipped(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String orderId = (String) payload.get("orderId");
        String trackingNumber = (String) payload.get("trackingNumber");
        String carrier = (String) payload.get("carrier");
        String estimatedDelivery = (String) payload.get("estimatedDelivery");

        log.info("Handling order shipped for user: {}, order: {}", userId, orderId);

        String email = getUserEmail(userId);
        String phone = getUserPhone(userId);

        sendEmail(
                userId,
                email,
                "Your Order Has Been Shipped - #" + orderId,
                buildOrderShippedEmailBody(orderId, carrier, trackingNumber, estimatedDelivery),
                "ORDER_SHIPPED"
        );

//        sendSms(
//                userId,
//                phone,
//                String.format("Order #%s shipped via %s. Track: %s",
//                        orderId, carrier, trackingNumber),
//                "ORDER_SHIPPED"
//        );
    }

    // ========== Helper Methods ==========
    private void sendEmail(String userId, String to, String subject,
                           String body, String eventType) {
        try {
            emailService.sendEmail(to, subject, body);
            logNotification(userId, "EMAIL", eventType, to, "SENT", null);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logNotification(userId, "EMAIL", eventType, to, "FAILED", e.getMessage());
            log.error("Failed to send email to: {}", to, e);
        }
    }

//    private void sendSms(String userId, String phoneNumber, String message,
//                         String eventType) {
//        try {
//            smsService.sendSms(phoneNumber, message);
//            logNotification(userId, "SMS", eventType, phoneNumber, "SENT", null);
//            log.info("SMS sent successfully to: {}", phoneNumber);
//        } catch (Exception e) {
//            logNotification(userId, "SMS", eventType, phoneNumber, "FAILED", e.getMessage());
//            log.error("Failed to send SMS to: {}", phoneNumber, e);
//        }
//    }

    private void logNotification(String userId, String channel, String eventType,
                                 String recipient, String status, String error) {
        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .channel(channel)
                .eventType(eventType)
                .recipient(recipient)
                .status(status)
                .errorMessage(error)
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    // ========== Email Body Templates ==========
    private String buildWelcomeEmailBody(String name, String email) {
        return String.format("""
            <html>
            <body>
                <h2>Welcome to %s!</h2>
                <p>Hi %s,</p>
                <p>Thank you for registering with us. We're excited to have you on board!</p>
                <p>Your account has been successfully created with email: %s</p>
                <p>Get started by exploring our platform.</p>
                <p>Best regards,<br>The %s Team</p>
            </body>
            </html>
            """, platformName, name, email, platformName);
    }

    private String buildVerificationEmailBody(String name) {
        return String.format("""
            <html>
            <body>
                <h2>Account Verified!</h2>
                <p>Hi %s,</p>
                <p>Your account has been successfully verified.</p>
                <p>You now have full access to all features.</p>
            </body>
            </html>
            """, name);
    }

    private String buildPaymentSuccessEmailBody(String transactionId, Double amount,
                                                String currency, String orderId) {
        return String.format("""
            <html>
            <body>
                <h2>Payment Successful</h2>
                <p>Your payment has been processed successfully.</p>
                <h3>Transaction Details:</h3>
                <ul>
                    <li>Transaction ID: %s</li>
                    <li>Amount: %.2f %s</li>
                    <li>Order ID: %s</li>
                </ul>
                <p>Thank you for your payment!</p>
            </body>
            </html>
            """, transactionId, amount, currency, orderId);
    }

    private String buildPaymentFailedEmailBody(Double amount, String currency,
                                               String orderId, String reason) {
        return String.format("""
            <html>
            <body>
                <h2>Payment Failed</h2>
                <p>Unfortunately, your payment could not be processed.</p>
                <h3>Details:</h3>
                <ul>
                    <li>Amount: %.2f %s</li>
                    <li>Order ID: %s</li>
                    <li>Reason: %s</li>
                </ul>
                <p>Please try again or contact support.</p>
            </body>
            </html>
            """, amount, currency, orderId, reason);
    }

    private String buildOrderCreatedEmailBody(String orderId, Double totalAmount,
                                              Integer itemCount) {
        return String.format("""
            <html>
            <body>
                <h2>Order Confirmed</h2>
                <p>Your order has been confirmed!</p>
                <h3>Order Details:</h3>
                <ul>
                    <li>Order ID: %s</li>
                    <li>Total Amount: %.2f</li>
                    <li>Items: %d</li>
                </ul>
                <p>We'll notify you when your order ships.</p>
            </body>
            </html>
            """, orderId, totalAmount, itemCount);
    }

    private String buildOrderShippedEmailBody(String orderId, String carrier,
                                              String trackingNumber, String estimatedDelivery) {
        return String.format("""
            <html>
            <body>
                <h2>Your Order Has Been Shipped!</h2>
                <p>Good news! Your order is on the way.</p>
                <h3>Shipping Details:</h3>
                <ul>
                    <li>Order ID: %s</li>
                    <li>Carrier: %s</li>
                    <li>Tracking Number: %s</li>
                    <li>Estimated Delivery: %s</li>
                </ul>
                <p>Track your shipment using the tracking number above.</p>
            </body>
            </html>
            """, orderId, carrier, trackingNumber, estimatedDelivery);
    }

    // Placeholder - In production, fetch from user service
    private String getUserEmail(String userId) {
        return "user@example.com"; // TODO: Fetch from user service
    }

    private String getUserPhone(String userId) {
        return "+1234567890"; // TODO: Fetch from user service
    }
}