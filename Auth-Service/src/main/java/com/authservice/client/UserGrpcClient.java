package com.authservice.client;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.dto.UserDTO;
import com.authservice.exception.UserNotFoundException;
import com.userservice.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

@Component
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public UserDTO getUserById(String id) {
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(id).build();
        try {
            UserResponse response = userServiceStub.getUserById(request);
            return mapToUserDTO(response);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                throw new UserNotFoundException("User not found for ID: " + id);
            }
            throw new RuntimeException("gRPC error: " + ex.getStatus().getDescription(), ex);
        }
    }

    public UserDTO getUserByEmail(String email) {
        GetUserByEmailRequest request = GetUserByEmailRequest.newBuilder().setEmail(email).build();
        try {
            UserResponse response = userServiceStub.getUserByEmail(request);
            return mapToUserDTO(response);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                throw new UserNotFoundException("User not found for email: " + email);
            }
            throw new RuntimeException("gRPC error: " + ex.getStatus().getDescription(), ex);
        }
    }

    public UserDTO getUserByUsername(String username) {
        GetUserByUsernameRequest request = GetUserByUsernameRequest.newBuilder().setUsername(username).build();
        try {
            UserResponse response = userServiceStub.getUserByUsername(request);
            return mapToUserDTO(response);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                throw new UserNotFoundException("User not found for username: " + username);
            }
            throw new RuntimeException("gRPC error: " + ex.getStatus().getDescription(), ex);
        }
    }

    public boolean updatePassword(ChangePasswordRequest request) {
        UpdatePasswordRequest grpcRequest = UpdatePasswordRequest.newBuilder()
                .setEmail(request.getEmail())
                .setNewPassword(request.getNewPassword())
                .setRepeatPassword(request.getRepeatPassword())
                .build();
        try {
            UpdatePasswordResponse grpcResponse = userServiceStub.updatePassword(grpcRequest);
            return grpcResponse.getSuccess();
        } catch (StatusRuntimeException ex) {
            // Handle specific error codes as needed
            throw new RuntimeException("gRPC error: " + ex.getStatus().getDescription(), ex);
        }
    }

    private UserDTO mapToUserDTO(UserResponse userResponse) {
        if (userResponse == null || userResponse.getId().isEmpty()) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(UUID.fromString(userResponse.getId()));
        dto.setUsername(userResponse.getUsername());
        dto.setEmail(userResponse.getEmail());
        dto.setPassword(userResponse.getPassword());
        dto.setRoles(new HashSet<>(userResponse.getRolesList()));
        dto.setGender(userResponse.getGender());
        dto.setDob(LocalDate.parse(userResponse.getDob()));
        return dto;
    }
}
