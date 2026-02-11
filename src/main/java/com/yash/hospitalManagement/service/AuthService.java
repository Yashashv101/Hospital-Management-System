package com.yash.hospitalManagement.service;

import com.yash.hospitalManagement.dto.LoginRequestDto;
import com.yash.hospitalManagement.dto.LoginResponseDto;
import com.yash.hospitalManagement.dto.SignupResponseDto;
import com.yash.hospitalManagement.entity.User;
import com.yash.hospitalManagement.repository.UserRepository;
import com.yash.hospitalManagement.security.AuthUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserRepository userRepository;
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication=authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
                );
        User user=(User)authentication.getPrincipal();
        String token=authUtil.generateToken(user);
        return new LoginResponseDto(token, user.getId());
    }

    public SignupResponseDto signup(LoginRequestDto signupRequestDto) {
        User user=userRepository.findByUsername(signupRequestDto.getUsername()).orElse(null);
        if(user!=null) throw new IllegalArgumentException("User already exists");

        user=userRepository.save(User.builder()
                .username(signupRequestDto.getUsername())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .build());
        return new SignupResponseDto(user.getId(),user.getUsername());
    }
}
