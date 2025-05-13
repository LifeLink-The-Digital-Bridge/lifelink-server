package com.authservice.client;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.dto.UserDTO;
import com.userservice.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.UUID;

@Component
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public UserDTO getUserById(String id) {
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(id).build();
        UserResponse response = userServiceStub.getUserById(request);
        return mapToUserDTO(response);
    }

    public UserDTO getUserByEmail(String email) {
        GetUserByEmailRequest request = GetUserByEmailRequest.newBuilder().setEmail(email).build();
        System.out.println("In email"+request.getEmail());
        UserResponse response = userServiceStub.getUserByEmail(request);
        return mapToUserDTO(response);
    }

    public UserDTO getUserByUsername(String username) {
        GetUserByUsernameRequest request = GetUserByUsernameRequest.newBuilder().setUsername(username).build();
        System.out.println("In User name :"+request.getUsername());
        UserResponse response = userServiceStub.getUserByUsername(request);
        return mapToUserDTO(response);
    }

    public boolean updatePassword(ChangePasswordRequest request) {
        UpdatePasswordRequest grpcRequest = UpdatePasswordRequest.newBuilder()
                .setEmail(request.getEmail())
                .setNewPassword(request.getNewPassword())
                .setRepeatPassword(request.getRepeatPassword())
                .build();

        UpdatePasswordResponse grpcResponse = userServiceStub.updatePassword(grpcRequest);
        return grpcResponse.getSuccess();
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
        return dto;
    }
}
