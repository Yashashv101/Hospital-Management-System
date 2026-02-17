package com.yash.hospitalManagement.service;

import com.yash.hospitalManagement.dto.LoginRequestDto;
import com.yash.hospitalManagement.dto.LoginResponseDto;
import com.yash.hospitalManagement.dto.SignupResponseDto;
import com.yash.hospitalManagement.entity.User;
import com.yash.hospitalManagement.entity.type.AuthProviderType;
import com.yash.hospitalManagement.entity.type.RoleType;
import com.yash.hospitalManagement.repository.UserRepository;
import com.yash.hospitalManagement.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;

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

    public User signUpInternal(LoginRequestDto signupRequestDto,AuthProviderType authProviderType,String providerId) {
        User user=userRepository.findByUsername(signupRequestDto.getUsername()).orElse(null);
        if(user!=null) throw new IllegalArgumentException("User already exists");

        user=User.builder()
                .username(signupRequestDto.getUsername())
                .providerId(providerId)
                .providerType(authProviderType)
                .roles(Set.of(RoleType.PATIENT))
                .build();
        if(authProviderType==AuthProviderType.EMAIL){
            user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        }
        return  userRepository.save(user);
    }

    public SignupResponseDto signUp(LoginRequestDto signupRequestDto) {
        User user=signUpInternal(signupRequestDto,AuthProviderType.EMAIL,null);
        return new SignupResponseDto(user.getId(),user.getUsername());
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User authUser, String registrationId) {
        AuthProviderType providerType=authUtil.getProviderType(registrationId);
        String providerId= authUtil.determineProviderIdFromOAuth2User(authUser,registrationId);
        User user=userRepository.findByProviderIdAndProviderType(providerId,providerType).orElse(null);
        String email=authUser.getAttribute("email");
        User emailUser=userRepository.findByUsername(email).orElse(null);
        if(user==null && emailUser==null){
            String username=authUtil.determineUsernameFromOAuth2User(authUser,registrationId,providerId);
           user=signUpInternal(new LoginRequestDto(username,null),providerType,providerId);
        }else if(user!=null){
            if(email!=null && !email.isBlank() && !email.equals(user.getUsername())){
                user.setUsername(email);
                userRepository.save(user);
            }
        }
        else{
            throw new BadCredentialsException("This email is already registered with provider: "+emailUser.getProviderType());
        }
        LoginResponseDto loginResponseDto=new LoginResponseDto(authUtil.generateToken(user),user.getId());
        return ResponseEntity.ok(loginResponseDto);
    }
}
