package com.trip.staysync.service;


import com.trip.staysync.dto.HotelDto;
import com.trip.staysync.dto.HotelInfoDto;
import com.trip.staysync.dto.RoomDto;
import com.trip.staysync.entity.Hotel;
import com.trip.staysync.entity.Room;
import com.trip.staysync.entity.User;
import com.trip.staysync.exception.ResouceNotFoundException;
import com.trip.staysync.exception.UnAuthorizedException;
import com.trip.staysync.repository.HotelRepository;
import com.trip.staysync.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.trip.staysync.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new Hotel with name: {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with ID: {}",hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not Found with ID: "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: "+id);
        }

        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Update hotel by ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not Found with ID: "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: "+id);
        }

        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);

    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not Found with ID: "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: "+id);
        }

        boolean exists = hotelRepository.existsById(id);
        if(!exists) throw new ResouceNotFoundException("Hotel not Found with ID: "+id);

        for(Room room: hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activeHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not Found with ID: "+ hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: "+ hotelId);
        }

        hotel.setActive(true);

        //assuming only do it once
        for(Room room : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

//    public method
    @Override
    public HotelInfoDto getHotelInfoById(long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not found with ID: "+hotelId));
        List<RoomDto> rooms = hotel.getRooms().stream()
                .map((element) -> modelMapper.map(element,RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);

    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all the hotels for the ADMIN user with ID : {}",user.getId());

        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels.stream()
                .map((element) -> modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }

}
