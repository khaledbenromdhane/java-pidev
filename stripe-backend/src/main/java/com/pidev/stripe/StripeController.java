package com.pidev.stripe;

import com.pidev.stripe.dto.CancelRequest;
import com.pidev.stripe.dto.CheckoutRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class StripeController {
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/api/stripe/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(@RequestBody CheckoutRequest request) {
        try {
            String url = stripeService.createCheckoutSession(request);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (StripeException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage() == null ? "Checkout failed" : ex.getMessage()));
        }
    }

    @GetMapping("/api/stripe/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam("sessionId") String sessionId) {
        try {
            return ResponseEntity.ok(stripeService.verifySession(sessionId));
        } catch (StripeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/api/stripe/cancel")
    public ResponseEntity<Void> cancel(@RequestBody CancelRequest request) {
        if (request.getParticipationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        stripeService.markCancelled(request.getParticipationId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/api/stripe/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> webhook(HttpServletRequest request, @RequestBody String payload) {
        String signature = request.getHeader("Stripe-Signature");
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            stripeService.handleWebhook(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/stripe/return/success")
    public String success(@RequestParam(value = "pid", required = false) String pid) {
        return "<html><body style='font-family:sans-serif;background:#111827;color:#fff;'>" +
            "<div style='max-width:560px;margin:40px auto;padding:20px;border-radius:12px;background:#0f172a;'>" +
            "<h2>Paiement confirme</h2>" +
            "<p>Vous pouvez fermer cette page.</p>" +
            "</div></body></html>";
    }

    @GetMapping("/stripe/return/cancel")
    public String cancelPage(@RequestParam(value = "pid", required = false) String pid) {
        return "<html><body style='font-family:sans-serif;background:#111827;color:#fff;'>" +
            "<div style='max-width:560px;margin:40px auto;padding:20px;border-radius:12px;background:#0f172a;'>" +
            "<h2>Paiement annule</h2>" +
            "<p>Vous pouvez fermer cette page.</p>" +
            "</div></body></html>";
    }
}
