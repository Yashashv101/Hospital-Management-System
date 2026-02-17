package com.yash.hospitalManagement.controller;

import com.yash.hospitalManagement.dto.LoginRequestDto;
import com.yash.hospitalManagement.dto.LoginResponseDto;
import com.yash.hospitalManagement.dto.SignupResponseDto;
import com.yash.hospitalManagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> sigup(@RequestBody LoginRequestDto signupRequestDto) {
        return ResponseEntity.ok(authService.signUp(signupRequestDto));
    }
}
