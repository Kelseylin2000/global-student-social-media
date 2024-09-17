package com.example.social_media.service.implement;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.social_media.dto.UserDto;
import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;
import com.example.social_media.exception.EmailAlreadyExistsException;
import com.example.social_media.exception.SignInFailException;
import com.example.social_media.mapper.UserMapper;
import com.example.social_media.model.entity.User;
import com.example.social_media.repository.mysql.UserRepository;
import com.example.social_media.security.JwtUtil;
import com.example.social_media.service.UserService;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService{

    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private UserRepository userRepository;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Value("${jwt.expiration}")
    private Long expiration;

    @Override
    @Transactional
    public AuthResponseDto signUp(SignUpRequestDto signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User newUser = new User();
        newUser.setProvider("native");
        newUser.setName(signUpRequest.getName());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        UserDto userDto = UserMapper.convertToDto(savedUser);
        String token = jwtUtil.generateToken(userDto);

        return new AuthResponseDto(token, expiration, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto signIn(SignInRequestDto signInRequest) {

        Optional<User> optionalUser = userRepository.findByEmail(signInRequest.getEmail());

        if (optionalUser.isEmpty()) {
            throw new SignInFailException("Invalid email");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
            throw new SignInFailException("Invalid password");
        }

        UserDto userDto = UserMapper.convertToDto(user);
        String token = jwtUtil.generateToken(userDto);

        return new AuthResponseDto(token, expiration, userDto);
    }

    @Override
    public String getUserNameByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getName() : null;
    }
} 
