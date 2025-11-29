package com.university.library.service;

import com.university.library.dto.UserRequest;
import com.university.library.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> listUsers();

    UserResponse getUser(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void deactivate(Long id);
}
