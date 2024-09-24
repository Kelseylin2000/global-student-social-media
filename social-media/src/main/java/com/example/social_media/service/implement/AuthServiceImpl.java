package com.example.social_media.service.implement;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;
import com.example.social_media.dto.user.UserDto;
import com.example.social_media.exception.EmailAlreadyExistsException;
import com.example.social_media.exception.SignInFailException;
import com.example.social_media.model.entity.User;
import com.example.social_media.model.node.UserNode;
import com.example.social_media.repository.mysql.UserRepository;
import com.example.social_media.repository.neo4j.UserNodeRepository;
import com.example.social_media.security.JwtUtil;
import com.example.social_media.service.AuthService;

import java.time.LocalDateTime;


@Service
public class AuthServiceImpl implements AuthService{

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserNodeRepository userNodeRepository;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserRepository userRepository, UserNodeRepository userNodeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userNodeRepository = userNodeRepository;
    }

    @Value("${jwt.expiration}")
    private Long expiration;

    @Override
    public AuthResponseDto signUp(SignUpRequestDto signUpRequest) {
    
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
    
        User savedUser = createUserInMySQL(signUpRequest);
        createUserInNeo4j(savedUser.getUserId(), signUpRequest.getName());
    
        UserDto userDto = new UserDto(savedUser.getUserId(), savedUser.getEmail());
        String token = jwtUtil.generateToken(userDto);
    
        return new AuthResponseDto(token, expiration, userDto);
    }
    
    @Transactional("mysqlTransactionManager")
    private User createUserInMySQL(SignUpRequestDto signUpRequest) {
        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User newUser = new User();
        newUser.setProvider("native");
        newUser.setName(signUpRequest.getName());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
    
        return userRepository.save(newUser);
    }
    
    @Transactional("neo4jTransactionManager")
    private void createUserInNeo4j(Long userId, String name) {
        UserNode newUserNode = new UserNode();
        newUserNode.setUserId(userId);
        newUserNode.setName(name);
    
        userNodeRepository.save(newUserNode);
    }
    

    @Override
    public AuthResponseDto signIn(SignInRequestDto signInRequest) {

        Optional<User> optionalUser = userRepository.findByEmail(signInRequest.getEmail());

        if (optionalUser.isEmpty()) {
            throw new SignInFailException("Invalid email");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
            throw new SignInFailException("Invalid password");
        }

        UserDto userDto = new UserDto(user.getUserId(), user.getEmail());
        String token = jwtUtil.generateToken(userDto);

        return new AuthResponseDto(token, expiration, userDto);
    }

    @Override
    public Long getCurrentUserId() {
        UserDto currentUser = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getUserId();
    }
} 
