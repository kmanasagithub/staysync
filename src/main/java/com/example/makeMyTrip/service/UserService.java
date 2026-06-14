package com.example.makeMyTrip.service;

import com.example.makeMyTrip.dto.ProfileUpdateRequestDto;
import com.example.makeMyTrip.dto.UserDto;
import com.example.makeMyTrip.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
