package com.trip.staysync.service;

import com.trip.staysync.dto.ProfileUpdateRequestDto;
import com.trip.staysync.dto.UserDto;
import com.trip.staysync.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
