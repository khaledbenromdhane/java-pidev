package com.pidev.stripe;

import com.pidev.stripe.dto.CheckoutRequest;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StripeService {
    private final ParticipationStatusRepository statusRepository;
    private final String webhookSecret;
    private final String successUrl;
    private final String cancelUrl;

    public StripeService(
            ParticipationStatusRepository statusRepository,
            @Value("${stripe.secret-key}") String secretKey,
            @Value("${stripe.webhook-secret}") String webhookSecret,
            @Value("${stripe.success-url}") String successUrl,
            @Value("${stripe.cancel-url}") String cancelUrl) {
        this.statusRepository = statusRepository;
        this.webhookSecret = webhookSecret;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        Stripe.apiKey = secretKey;
    }

    public String createCheckoutSession(CheckoutRequest request) throws StripeException {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key missing");
        }
        if (request.getEventName() == null || request.getEventName().isBlank()) {
            throw new IllegalArgumentException("Event name missing");
        }
        if (request.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price missing");
        }
        if (request.getPlaces() == null || request.getPlaces() <= 0) {
            throw new IllegalArgumentException("Places missing");
        }

        long unitAmount = Math.round(request.getUnitPrice() * 100);
        if (unitAmount <= 0) {
            throw new IllegalArgumentException("Invalid unit amount");
        }

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(request.getEventName())
                        .setDescription("Reservation #" + request.getParticipationId())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("eur")
                        .setUnitAmount(unitAmount)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(request.getPlaces().longValue())
                .setPriceData(priceData)
                .build();

        String success = successUrl + "?session_id={CHECKOUT_SESSION_ID}&pid=" + request.getParticipationId();
        String cancel = cancelUrl + "?pid=" + request.getParticipationId();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(success)
                .setCancelUrl(cancel)
                .addLineItem(lineItem)
                .putMetadata("participation_id", String.valueOf(request.getParticipationId()))
                .putMetadata("event_id", String.valueOf(request.getEventId()))
                .putMetadata("places", String.valueOf(request.getPlaces()))
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public Map<String, Object> verifySession(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        boolean paid = "paid".equalsIgnoreCase(session.getPaymentStatus());
        Integer participationId = parseParticipationId(session.getMetadata());
        if (paid && participationId != null) {
            statusRepository.updateStatus(participationId, "Confirmée");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paid", paid);
        result.put("participationId", participationId);
        return result;
    }

    public void markCancelled(int participationId) {
        statusRepository.updateStatus(participationId, "Annulée");
    }

    public void handleWebhook(String payload, String signatureHeader) throws SignatureVerificationException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return;
        }

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> dataObject = dataObjectDeserializer.getObject();

        if (dataObject.isEmpty()) {
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSession((Session) dataObject.get(), true);
            case "checkout.session.expired" -> handleCheckoutSession((Session) dataObject.get(), false);
            default -> {
                // ignore
            }
        }
    }

    private void handleCheckoutSession(Session session, boolean paid) {
        Integer participationId = parseParticipationId(session.getMetadata());
        if (participationId == null) {
            return;
        }
        statusRepository.updateStatus(participationId, paid ? "Confirmée" : "Annulée");
    }

    private Integer parseParticipationId(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        String value = metadata.get("participation_id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
