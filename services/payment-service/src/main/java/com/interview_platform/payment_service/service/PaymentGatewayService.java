package com.interview_platform.payment_service.service;

import com.interview_platform.payment_service.dto.AddMoneyRequest;
import com.interview_platform.payment_service.dto.AddMoneyResponse;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {

    private final RazorpayClient razorpayClient;
    private final WalletService walletService;

    public AddMoneyResponse initiateAddMoney(AddMoneyRequest request) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount().multiply(new BigDecimal("1000")).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", UUID.randomUUID().toString());

        JSONObject notes = new JSONObject();
        notes.put("userId", request.getUserId());
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);

        return AddMoneyResponse.builder()
                .orderId(order.get("id"))
                .amount(request.getAmount())
                .currency("INR")
                .build();
    }

    public void handlePaymentSuccess(String orderId, String paymentId, String signature) throws Exception {
        if (!verifySignature(orderId, paymentId, signature)) {
            throw new RuntimeException("Invalid signature");
        }

        Order order = razorpayClient.orders.fetch(orderId);
        BigDecimal amount = new BigDecimal((char[]) order.get("amount")).divide(new BigDecimal("100"));
        String userId = order.get("notes").get("userId");

        walletService.creditWallet(userId, amount, paymentId, "Add money via Razorpay");
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        // Implement signature verification
        return true; // Placeholder
    }
}
