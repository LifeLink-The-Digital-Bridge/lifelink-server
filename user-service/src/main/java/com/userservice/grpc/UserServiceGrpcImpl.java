package com.userservice.grpc;

import com.userservice.dto.ChangePasswordRequest;
import com.userservice.dto.UserDTOPassword;
import com.userservice.service.UserService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    public UserServiceGrpcImpl(UserService userService) {
        System.out.println("UserServiceGrpcImpl bean CREATED!");
        this.userService = userService;
    }

    private final UserService userService;


    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        UserDTOPassword user = userService.getUserByEmail(request.getEmail());
        System.out.println("In getUserByEmail");
        System.out.println(user.toString());
        UserResponse response = mapToGrpcUser(user);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, StreamObserver<UserResponse> responseObserver) {
        UserDTOPassword user = userService.getUserByUsername(request.getUsername());
        System.out.println("In getUserByUsername");
        System.out.println(user.toString());
        UserResponse response = mapToGrpcUser(user);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UserDTOPassword user = userService.getUserById(UUID.fromString(request.getId()));
        System.out.println("In getUserById");
        System.out.println(user.toString());
        UserResponse response = mapToGrpcUser(user);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request, StreamObserver<UpdatePasswordResponse> responseObserver) {
        boolean result = userService.updatePassword(
                new ChangePasswordRequest(
                        request.getEmail(),
                        request.getNewPassword(),
                        request.getRepeatPassword()
                )
        );
        UpdatePasswordResponse response = UpdatePasswordResponse.newBuilder()
                .setSuccess(result)
                .setMessage(result ? "Password updated successfully" : "Password mismatch or user not found")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



    private UserResponse mapToGrpcUser(UserDTOPassword user) {
        return UserResponse.newBuilder()
                .setId(user.getId().toString())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .addAllRoles(user.getRoles())
                .build();
    }
}
