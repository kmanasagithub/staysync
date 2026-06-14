package com.example.makeMyTrip.strategies;

import com.example.makeMyTrip.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
