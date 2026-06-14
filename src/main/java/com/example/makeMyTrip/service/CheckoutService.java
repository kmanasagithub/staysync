package com.example.makeMyTrip.service;

import com.example.makeMyTrip.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
