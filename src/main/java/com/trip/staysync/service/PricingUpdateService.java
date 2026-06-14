package com.trip.staysync.service;

import com.trip.staysync.entity.Hotel;
import com.trip.staysync.entity.HotelMinPrice;
import com.trip.staysync.entity.Inventory;
import com.trip.staysync.repository.HotelMinPriceRepository;
import com.trip.staysync.repository.HotelRepository;
import com.trip.staysync.repository.InventoryRepository;
import com.trip.staysync.strategies.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

//    Scheduler to update the inventory and HotelMinPrice tables every year
    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        int page = 0;
        int batchSize = 100;

        while(true) {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));

            if(hotelPage.isEmpty()) {
                break;
            }

            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }

    }

    private void updateHotelPrices(Hotel hotel) {
        log.info("Updating Hotel Prices for hotel with ID: {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        updateInventoryPrices(inventoryList);

        updateHotelMinPrices(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrices(Hotel hotel,List<Inventory> inventoryList,LocalDate startDate,LocalDate endDate) {
        //Compute minimum price per day for the hotel
        Map<LocalDate,BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        //Prepare hotelPrice entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices .forEach((date,price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        //Save all repositories entites in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {
       inventoryList.forEach(inventory -> {
           BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
           inventory.setPrice(dynamicPrice);
       });
       inventoryRepository.saveAll(inventoryList);
    }


}
