package com.trip.staysync.service;

import com.trip.staysync.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
