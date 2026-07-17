package com.example.bankDemo.security.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.auth.UserInfoResponse;
import com.example.bankDemo.entity.Customer;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.enums.UserRole;
import com.example.bankDemo.repository.CustomerRepository;
import com.example.bankDemo.security.entity.User;
import com.example.bankDemo.security.model.AuthRequest;
import com.example.bankDemo.security.model.AuthResponse;
import com.example.bankDemo.security.model.SignUpResponse;
import com.example.bankDemo.security.model.UserDetailsImpl;
import com.example.bankDemo.security.repository.UserRepository;
import com.example.bankDemo.security.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final CustomerRepository customerRepository;

    @Override
    public ApiResponse<Object> login(AuthRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwt);
        authResponse.setUsername(userDetails.getUsername());
        authResponse.setExpirationTime(new Date(System.currentTimeMillis() + 720000));

        // Set cookie here
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return new ApiResponse<>(authResponse, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
    }


    @Override
    public ApiResponse<Object> adminRegister(AuthRequest authRequest) {
        User user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setRole(UserRole.ADMIN);

        SignUpResponse response = new SignUpResponse();
        response.setRole(user.getRole());
        response.setUsername(user.getUsername());
        userRepository.save(user);

        return new ApiResponse<>(response, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
    }
    @Override
    public ApiResponse<Object> getCurrentUser(Authentication authentication) {
        try{
            Optional<User> optionalUser = userRepository.findByUsername(authentication.getName());
            if(optionalUser.isEmpty()){
                return new ApiResponse<>(null, ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            User user = optionalUser.get();

            Optional<Customer> optionalCustomer = customerRepository.findByEmail(user.getUsername());
            if(optionalCustomer.isEmpty()){
                return new ApiResponse<>(null, ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Customer customer = optionalCustomer.get();

            return new ApiResponse<>(UserInfoResponse.builder()
                    .username(customer.getEmail())
                    .firstName(customer.getFirstName())
                    .role(user.getRole().name())
                    .customerPublicId(customer.getPublicId())
                    .build(), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e. getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }
}
