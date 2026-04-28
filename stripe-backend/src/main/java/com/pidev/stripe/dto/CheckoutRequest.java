package com.pidev.stripe.dto;

public class CheckoutRequest {
    private Integer participationId;
    private Integer eventId;
    private String eventName;
    private Double unitPrice;
    private Integer places;

    public Integer getParticipationId() {
        return participationId;
    }

    public void setParticipationId(Integer participationId) {
        this.participationId = participationId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }
}
