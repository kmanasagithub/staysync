package com.example.makeMyTrip.security;

import com.example.makeMyTrip.dto.LoginDto;
import com.example.makeMyTrip.dto.SignUpRequestDto;
import com.example.makeMyTrip.dto.UserDto;
import com.example.makeMyTrip.entity.User;
import com.example.makeMyTrip.entity.enums.Role;
import com.example.makeMyTrip.exception.ResouceNotFoundException;
import com.example.makeMyTrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDto signup(SignUpRequestDto signUpRequestDto) {

        User user =  userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if(user != null) {
            throw new RuntimeException("user is already present with same email id");
        }

        User newUser = modelMapper.map(signUpRequestDto,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];

        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateAccessToken(user);

        return arr;

    }

    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(id).orElseThrow(
                () -> new ResouceNotFoundException("User not Fund with the id: "+id)
        );

        return jwtService.generateRefreshToken(user);
    }
}
