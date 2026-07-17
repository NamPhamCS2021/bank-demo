package com.example.bankDemo.security.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.entity.Customer;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.security.entity.User;
import com.example.bankDemo.security.model.UserDTO;
import com.example.bankDemo.security.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public ApiResponse<Object> findByUsername(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
        }
        User userR = optionalUser.get();
        Customer customer = userR.getCustomer();
        if (customer == null) {
            return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
        }
        UserDTO userDTO = new UserDTO(userR.getUsername(), userR.getRole(), userR.getCustomer().getCreatedDate());
        return new ApiResponse<>(userDTO, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
    }
}
